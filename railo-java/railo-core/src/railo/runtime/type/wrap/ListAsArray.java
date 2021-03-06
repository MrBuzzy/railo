package railo.runtime.type.wrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import org.apache.poi.ss.formula.functions.T;

import railo.commons.lang.CFTypes;
import railo.runtime.PageContext;
import railo.runtime.converter.LazyConverter;
import railo.runtime.dump.DumpData;
import railo.runtime.dump.DumpProperties;
import railo.runtime.dump.DumpUtil;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.PageException;
import railo.runtime.op.Caster;
import railo.runtime.op.Duplicator;
import railo.runtime.type.Array;
import railo.runtime.type.ArrayImpl;
import railo.runtime.type.Collection;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.Sizeable;
import railo.runtime.type.Struct;
import railo.runtime.type.dt.DateTime;
import railo.runtime.type.it.EntryIterator;
import railo.runtime.type.it.KeyIterator;
import railo.runtime.type.it.StringIterator;
import railo.runtime.type.util.ArrayUtil;
import railo.runtime.type.util.MemberUtil;

/**
 * 
 */
public class ListAsArray implements Array,List,Sizeable {

	protected List list;
	
	private ListAsArray(List list) {
		this.list=list;
	}
	
	public static Array toArray(List list) {
		if(list instanceof ArrayAsList) return ((ArrayAsList)list).array;
		if(list instanceof Array) return (Array) list;
		return new ListAsArray(list);
	}
	
	
	/**
	 * @see railo.runtime.type.Array#append(java.lang.Object)
	 */
	public Object append(Object o) throws PageException {
		list.add(o);
		return o;
	}
	
	
	/**
	 * @see railo.runtime.type.Array#appendEL(java.lang.Object)
	 */
	public Object appendEL(Object o) {
		list.add(o);
		return o;
	}

	/**
	 * @see railo.runtime.type.Array#containsKey(int)
	 */
	public boolean containsKey(int index) {
		return get(index-1,null)!=null;
	}

	/**
	 * @see railo.runtime.type.Array#get(int, java.lang.Object)
	 */
	public Object get(int key, Object defaultValue) {
		try {
			Object rtn = list.get(key-1);
			if(rtn==null) return defaultValue;
			return rtn;
		}
		catch(Throwable t) {
			return defaultValue;
		}
	}

	/**
	 * @see railo.runtime.type.Array#getE(int)
	 */
	public Object getE(int key) throws PageException {
		try {
			Object rtn = list.get(key-1);
			if(rtn==null) throw new ExpressionException("Element at position ["+key+"] does not exist in list");
			return rtn;
		}
		catch(Throwable t) {
			throw new ExpressionException("Element at position ["+key+"] does not exist in list",t.getMessage());
		}
	}

	/**
	 * @see railo.runtime.type.Array#getDimension()
	 */
	public int getDimension() {
		return 1;
	}

	public boolean insert(int key, Object value) throws PageException {
		try {
		list.add(key-1, value);
		}
		catch(Throwable t) {
			throw new ExpressionException("can't insert value to array at position "+key+", array goes from 1 to "+size());
		}
		return true;
	}

	public int[] intKeys() {
		ListIterator lit = list.listIterator();
		ArrayList keys = new ArrayList();
		int index=0;
		Object v;
		while(lit.hasNext()) {
			index=lit.nextIndex()+1;
			v=lit.next();
			if(v!=null)keys.add(Integer.valueOf(index));
		}
		int[] intKeys = new int[keys.size()];
		Iterator it = keys.iterator();
		index=0;
		while(it.hasNext()) {
			intKeys[index++]=((Integer)it.next()).intValue();
		}
		
		return intKeys;
	}

	/**
	 * @see railo.runtime.type.Array#prepend(java.lang.Object)
	 */
	public Object prepend(Object o) throws PageException {
		list.add(0,o);
		return o;
	}

	public Object removeE(int key) throws PageException {
		try {
		return list.remove(key-1);
		}
		catch(Throwable t) {
			throw new ExpressionException("can not remove Element at position ["+key+"]",t.getMessage());
		}
	}

	/**
	 * @see railo.runtime.type.Array#removeEL(int)
	 */
	public Object removeEL(int key) {
		try {
			return removeE(key);
		} catch (PageException e) {
			return null;
		}
	}

	public void resize(int to) throws PageException {
		while(size()<to)list.add(null);
	}

	/**
	 * @see railo.runtime.type.Array#setE(int, java.lang.Object)
	 */
	public Object setE(int key, Object value) throws PageException {
		if(key<=size()) {
			try {
			list.set(key-1, value);
			}
			catch(Throwable t) {
				throw new ExpressionException("can not set Element at position ["+key+"]",t.getMessage());
			}
			
		}
		else {
			while(size()<key-1)list.add(null);
			list.add(value);
		}
		return value;
	}

	/**
	 * @see railo.runtime.type.Array#setEL(int, java.lang.Object)
	 */
	public Object setEL(int key, Object value) {
		try {
			return setE(key, value);
		} catch (Throwable t) {}
		return value;
	}

	@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		sort(ArrayUtil.toComparator(null, sortType, sortOrder, false));
	}

	@Override
	public synchronized void sort(Comparator comp) throws PageException {
		if(getDimension()>1)
			throw new ExpressionException("only 1 dimensional arrays can be sorted");
		Collections.sort(list,comp);
	}

	/**
	 * @see railo.runtime.type.Array#toArray()
	 */
	public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * @see railo.runtime.type.Array#toArrayList()
	 */
	public ArrayList toArrayList() {
		return new ArrayList(list);
	}

	/**
	 * @see railo.runtime.type.Collection#clear()
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * @see railo.runtime.type.Collection#containsKey(java.lang.String)
	 */
	public boolean containsKey(String key) {
		return get(key,null)!=null;
	}

	/**
	 * @see railo.runtime.type.Collection#containsKey(railo.runtime.type.Collection.Key)
	 */
	public boolean containsKey(Key key) {
		return get(key,null)!=null;
	}

	/**
	 * @see railo.runtime.type.Collection#duplicate(boolean)
	 */
	public Collection duplicate(boolean deepCopy) {new ArrayImpl().duplicate(deepCopy);
		return new ListAsArray((List)Duplicator.duplicate(list,deepCopy));
	}

	

	/**
	 * @see railo.runtime.type.Collection#get(java.lang.String)
	 */
	public Object get(String key) throws PageException {
		return getE(Caster.toIntValue(key));
	}

	/**
	 * @see railo.runtime.type.Collection#get(railo.runtime.type.Collection.Key)
	 */
	public Object get(Key key) throws PageException {
		return get(key.getString());
	}

	/**
	 * @see railo.runtime.type.Collection#get(java.lang.String, java.lang.Object)
	 */
	public Object get(String key, Object defaultValue) {
		double index=Caster.toIntValue(key,Integer.MIN_VALUE);
		if(index==Integer.MIN_VALUE) return defaultValue;
	    return get((int)index,defaultValue);
	}

	/**
	 * @see railo.runtime.type.Collection#get(railo.runtime.type.Collection.Key, java.lang.Object)
	 */
	public Object get(Key key, Object defaultValue) {
		return get(key.getString(),defaultValue);
	}

	/**
	 * @see railo.runtime.type.Collection#keys()
	 */
	public Key[] keys() {
		int[] intKeys = intKeys();
		Collection.Key[] keys = new Collection.Key[intKeys.length];
		for(int i=0;i<intKeys.length;i++) {
			keys[i]=KeyImpl.init(Caster.toString(intKeys[i]));
		}
		return keys;
	}

	/**
	 *
	 * @see railo.runtime.type.Collection#remove(railo.runtime.type.Collection.Key)
	 */
	public Object remove(Key key) throws PageException {
		return removeE(Caster.toIntValue(key.getString()));
	}

	/**
	 * @see railo.runtime.type.Collection#removeEL(java.lang.String)
	 */
	public Object removeEL(Key key) {
		double index=Caster.toIntValue(key.getString(),Integer.MIN_VALUE);
		if(index==Integer.MIN_VALUE) return null;
	    return removeEL((int)index);
	}

	/**
	 *
	 * @see railo.runtime.type.Collection#set(java.lang.String, java.lang.Object)
	 */
	public Object set(String key, Object value) throws PageException {
		return setE(Caster.toIntValue(key),value);
	}

	/**
	 *
	 * @see railo.runtime.type.Collection#set(railo.runtime.type.Collection.Key, java.lang.Object)
	 */
	public Object set(Key key, Object value) throws PageException {
		return set(key.getString(),value);
	}

	/**
	 * @see railo.runtime.type.Collection#setEL(java.lang.String, java.lang.Object)
	 */
	public Object setEL(String key, Object value) {
		double index=Caster.toIntValue(key,Integer.MIN_VALUE);
		if(index==Integer.MIN_VALUE) return value;
	    return setEL((int)index,value);
	}

	/**
	 * @see railo.runtime.type.Collection#setEL(railo.runtime.type.Collection.Key, java.lang.Object)
	 */
	public Object setEL(Key key, Object value) {
		return setEL(key.getString(), value);
	}

	/**
	 * @see railo.runtime.type.Collection#size()
	 */
	public int size() {
		return list.size();
	}

	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(list, pageContext,maxlevel,dp);
	}

	/**
	 * @see railo.runtime.type.Iteratorable#iterator()
	 */
	public Iterator iterator() {
		return list.iterator();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}
    
    @Override
	public Iterator<String> keysAsStringIterator() {
    	return new StringIterator(keys());
    }
	
	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this,keys());
	}

	/**
     * @see railo.runtime.op.Castable#castToString()
     */
    public String castToString() throws PageException {
        throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to String",
          "Use Built-In-Function \"serialize(Array):String\" to create a String from Array");
    }

	/**
	 * @see railo.runtime.op.Castable#castToString(java.lang.String)
	 */
	public String castToString(String defaultValue) {
		return defaultValue;
	}


    /**
     * @see railo.runtime.op.Castable#castToBooleanValue()
     */
    public boolean castToBooleanValue() throws PageException {
        throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a boolean value");
    }
    
    /**
     * @see railo.runtime.op.Castable#castToBoolean(java.lang.Boolean)
     */
    public Boolean castToBoolean(Boolean defaultValue) {
        return defaultValue;
    }


    /**
     * @see railo.runtime.op.Castable#castToDoubleValue()
     */
    public double castToDoubleValue() throws PageException {
        throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a number value");
    }
    
    /**
     * @see railo.runtime.op.Castable#castToDoubleValue(double)
     */
    public double castToDoubleValue(double defaultValue) {
        return defaultValue;
    }


    /**
     * @see railo.runtime.op.Castable#castToDateTime()
     */
    public DateTime castToDateTime() throws PageException {
        throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a Date");
    }
    
    /**
     * @see railo.runtime.op.Castable#castToDateTime(railo.runtime.type.dt.DateTime)
     */
    public DateTime castToDateTime(DateTime defaultValue) {
        return defaultValue;
    }

	/**
	 * @see railo.runtime.op.Castable#compare(boolean)
	 */
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a boolean value");
	}

	/**
	 * @see railo.runtime.op.Castable#compareTo(railo.runtime.type.dt.DateTime)
	 */
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a DateTime Object");
	}

	/**
	 * @see railo.runtime.op.Castable#compareTo(double)
	 */
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a numeric value");
	}

	/**
	 * @see railo.runtime.op.Castable#compareTo(java.lang.String)
	 */
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a String");
	}

	/**
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return LazyConverter.serialize(this);
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return duplicate(true);
	}

	/**
	 * @see java.util.List#addEntry(E)
	 */
	public boolean add(Object o) {
		return list.add(o);
	}

	/**
	 * @see java.util.List#add(int, E)
	 */
	public void add(int index, Object element) {
		list.add(index, element);
	}

	/**
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(java.util.Collection c) {
		return list.addAll(c);
	}

	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, java.util.Collection c) {
		return list.addAll(index, c);
	}

	/**
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return list.contains(o);
	}

	/**
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(java.util.Collection c) {
		return list.contains(c);
	}

	/**
	 * @see java.util.List#get(int)
	 */
	public Object get(int index) {
		return list.get(index);
	}

	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	/**
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	/**
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		return list.listIterator();
	}

	/**
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		return list.listIterator(index);
	}

	/**
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return list.remove(o);
	}

	/**
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		return list.remove(index);
	}

	/**
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(java.util.Collection c) {
		return list.removeAll(c);
	}

	/**
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(java.util.Collection c) {
		return list.retainAll(c);
	}

	/**
	 * @see java.util.List#set(int, E)
	 */
	public Object set(int index, Object element) {
		return list.set(index, element);
	}

	/**
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/**
	 * @see java.util.List#toArray(T[])
	 */
	public Object[] toArray(Object[] a) {
		return list.toArray(a);
	}


	/**
	 * @see railo.runtime.type.Array#toList()
	 */
	public List toList() {
		return this;
	}

	public Iterator valueIterator() {
		return list.iterator();
	}

	/**
	 * @see railo.runtime.type.Sizeable#sizeOf()
	 */
	public long sizeOf() {
		return ArrayUtil.sizeOf(list);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return get(key);
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array");
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return MemberUtil.callWithNamedValues(pc,this,methodName,args, CFTypes.TYPE_ARRAY, "array");
	}

	@Override
	public java.util.Iterator<Object> getIterator() {
    	return valueIterator();
    } 
}
