<cfscript>
component extends="Debug" {
	NL="
";
	fields=array(
		
		
		
		group("Custom Debugging Output","Define what is outputted",3)

		


		,field("General Debug Information ","general",true,false,
				"Select this option to show general information about this request.","checkbox")
		
		,field("Minimal Execution Time","minimal","0",true,
				"Execution times for templates, includes, modules, custom tags, and component method calls. Outputs only templates taking longer than the time (in ms) defined above.","text40")
		
		,field("Scope Variables","scopes","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL",true,"Select this option to show the content of the corresponding Scope.","checkbox","Application,CGI,Client,Cookie,Form,Request,Server,Session,URL")
		
		,field("Database Activity","database",true,false,"Select this option to show the database activity for the SQL Query events and Stored Procedure events.","checkbox")
		
		,field("Exceptions","exception",true,false,"Select this option to output all exceptions raised for the request. ","checkbox")
		
		,field("Tracing","tracing",true,false,"Select this option to show trace event information. Tracing lets a developer track program flow and efficiency through the use of the CFTRACE tag or the TraceObject Function.","checkbox")
		
		,field("Timer","timer",true,false,"Select this option to show timer event information. Timers let a developer track the execution time of the code between the start and end tags of the CFTIMER tag. ","checkbox")
		
	);
    
    /**
	* return the title of this debug type
	*/
	function getLabel() {
		return "Comment";
	}
	
	/**
	* return the description of this debug type
	*/
	function getDescription() {
		return "Outputs the debugging information as HTML Comment, only visible inside the HTML Source Code.";
	}
	
	/**
	* return the unique identifier for this debug type
	*/
	function getId() {
		return "railo-comment";
	}
	
	
	/**
	* validates settings done by the user
	* @param custom settings done by the user to validate
	*/
	function onBeforeUpdate(struct custom) {
		
	}
	
	
	/**
	* output the debugging information
	* @param custom settings done by the user
	*/
	function output(struct custom) {
		admin action="getDebugData" returnVariable="local.debugging";
		
		writeOutput("<!--"&NL);
 		echo("=================================================================================="&NL);
        echo("=========================== RAILO DEBUGGING INFORMATION =========================="&NL);
 		echo("=================================================================================="&NL&NL);
        
	// GENERAL
 		if(structKeyExists(custom,"general") && custom.general) {
			echo(server.coldfusion.productname);
			if(StructKeyExists(server.railo,'versionName'))
				echo('('&server.railo.versionName&')');
			
			echo(" "&ucFirst(server.coldfusion.productlevel));
			echo(" "&uCase(server.railo.state));
			echo(" "&server.railo.version);
			echo(' (CFML Version '&server.ColdFusion.ProductVersion&')');
			echo(NL);
			
			echo("Template: #cgi.SCRIPT_NAME# (#getBaseTemplatePath()#)");
			echo(NL);
			
			echo("Time Stamp: #LSDateFormat(now())# #LSTimeFormat(now())#");
			echo(NL);
			
			echo("Time Zone: #getTimeZone()#");
			echo(NL);
			
			echo("Locale: #ucFirst(getLocale())#");
			echo(NL);
			
			echo("User Agent: #cgi.http_user_agent#");
			echo(NL);
			
			echo("Remote IP: #cgi.remote_addr#");
			echo(NL);
			
			echo("Host Name: #cgi.server_name#");
			echo(NL);
			
			if(StructKeyExists(server.os,"archModel") and StructKeyExists(server.java,"archModel")) {
				echo("Architecture: ");
				if(server.os.archModel NEQ server.os.archModel)
					echo("OS #server.os.archModel#bit/JRE #server.java.archModel#bit");
				else 
					echo("#server.os.archModel#bit");
				echo(NL);
			}
 		}
		
	// Pages
		var pages=duplicate(debugging.pages);
        if(structKeyExists(custom,"minimal") && custom.minimal>0) {
            for(var row=pages.recordcount;row>0;row--){
                if(pages.total[row]<custom.minimal)
                    queryDeleteRow(pages,row);
            }
		}
		print("Pages",array('src','count','load','query','app','total'),pages);
	 	
	// DATABASE
		if(structKeyExists(custom,"database") && custom.database && debugging.queries.recordcount)
			print("Queries",array('src','datasource','name','sql','time','count'),debugging.queries);
			
	// TIMER
	 	if(structKeyExists(custom,"timer") && custom.timer && debugging.timers.recordcount)
			print("Timers",array('template','label','time'),debugging.timers);
	
	// TRACING
	 	if(structKeyExists(custom,"tracing") && custom.tracing && debugging.traces.recordcount)
			print("Trace Points",array('template','type','category','text','line','action','varname','varvalue','time'),debugging.traces);
		
	// EXCEPTION
		if(structKeyExists(custom,"exception") && custom.exception && arrayLen(debugging.exceptions)) {
			var qry=queryNew("type,message,detail,template")
			var len=arrayLen(debugging.exceptions);
			QueryAddRow(qry,len);
			for(var row=1;row<=len;row++){
				local.sct=debugging.exceptions[row];
				QuerySetCell(qry,"type",sct.type,row);
				QuerySetCell(qry,"message",sct.message,row);
				QuerySetCell(qry,"detail",sct.detail,row);
				QuerySetCell(qry,"template",sct.tagcontext[1].template&":"&sct.tagcontext[1].line,row);
			}
			//dump(qry);
			print("Caught Exceptions",array('type','message','detail','template'),qry);
		}
        
        
	// SCOPES   
     	scopes=["Application","CGI","Client","Cookie","Form","Request","Server","Session","URL"];
		if(not structKeyExists(custom,"scopes"))custom.scopes="";
		if(len(custom.scopes)) {
        echo("=================================================================================="&NL);
        echo(" SCOPES"&NL);
        echo("=================================================================================="&NL);
        
            for(var i=1;i<=arrayLen(scopes);i++){
            	local.name=scopes[i];
                if(!listFindNoCase(custom.scopes,name)) continue;
            	var doPrint=true;
				try{
					scp=evaluate(name);
   				}
                catch(any e){
                	doPrint=false;
                }
                
                if(doPrint and structCount(scp)) {
                	echo(uCase(name)&" SCOPE"&NL);
                    var keys=structKeyArray(scp);
                    for(var y=1;y<=arrayLen(keys);y++){
                    	local.key=keys[y];
                    	echo("- "&key&"=");
                        if(IsSimpleValue(scp[key]))				echo(scp[key]);
						else if(isArray(scp[key]))				echo('Array (#arrayLen(scp[key])#)');
						else if(isValid('component',scp[key]))	echo('Component (#GetMetaData(scp[key]).name#)');
						else if(isStruct(scp[key]))				echo('Struct (#StructCount(scp[key])#)');
						else if(IsQuery(scp[key]))				echo('Query (#scp[key].recordcount#)');
						else {
                        	echo('Complex type');
						}
                        echo(NL);
                    }
                }
                
            }
        }
			
        
        
        /*
        <p class="cfdebug"><hr/><b class="cfdebuglge"><a name="cfdebug_scopevars">Scope Variables</a></b></p>
<cfloop list="#scopes#" index="name"><cfif not ListFindNoCase(custom.scopes,name)><cfcontinue></cfif>


<cfif doPrint and structCount(scp)>
<pre><b>#name# Variables:</b><cftry><cfloop index="key" list="#ListSort(StructKeyList(scp),"textnocase")#">
#(key)#=<cftry><cfif IsSimpleValue(scp[key])>#scp[key]#<!--- 
---><cfelseif isArray(scp[key])>Array (#arrayLen(scp[key])#)<!--- 
---><cfelseif isValid('component',scp[key])>Component (#GetMetaData(scp[key]).name#)<!--- 
---><cfelseif isStruct(scp[key])>Struct (#StructCount(scp[key])#)<!--- 
---><cfelseif IsQuery(scp[key])>Query (#scp[key].recordcount#)<!--- 
---><cfelse>Complex type</cfif><cfcatch></cfcatch></cftry></cfloop><cfcatch>error (#cfcatch.message#) occurred while displaying Scope #name#</cfcatch></cftry>
</pre>
</cfif>
</cfloop>
</cfif>
        */
		
		writeOutput(NL& "-->");
	}
    
    
 	
	
	private function print(string title,array labels, query data) {
		
		// get maxlength of columns
		var lengths=array();
		var i=1;
		var y=1;
		var tmp=0;
		var total=1;
		var collen=arrayLen(labels);
		for(;i LTE collen;i=i+1) {
			lengths[i]=len(labels[i]);
			for(y=1;y LTE data.recordcount;y=y+1) {
			
				data[labels[i]][y]=trim(rereplace(data[labels[i]][y],"[[:space:]]+"," ","all"));
			
				tmp=len(data[labels[i]][y]);
				if(tmp GT lengths[i])lengths[i]=tmp;
			}
			lengths[i]=lengths[i]+3;
			total=total+lengths[i];
		}
		
		// now wrie out
		writeOutput(NL);
		writeOutput(RepeatString("=",total)&NL);
		writeOutput(ljustify(" "&ucase(title)&" " ,total));
		writeOutput(NL);
		writeOutput(RepeatString("=",total)&NL);
		for(y=1;y LTE collen;y=y+1) {
			writeOutput(ljustify("| "&uCase(labels[y])&" " ,lengths[y]));
		}
		writeOutput("|"&NL);
		
		for(i=1;i LTE data.recordcount;i=i+1) {
			writeOutput(RepeatString("-",total)&NL);
			for(y=1;y LTE collen;y=y+1) {
				writeOutput(ljustify("| "&data[labels[y]][i]&" " ,lengths[y]));
			}
			writeOutput("|"&NL);
		}
		writeOutput(RepeatString("=",total)&NL&NL);
 	}   
}
</cfscript>