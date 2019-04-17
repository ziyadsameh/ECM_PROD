require(["dojo/_base/declare",
         "dojo/_base/lang"], 
function(declare, lang) {
	
	
	
	/*lang.setObject("LogDocumentPrintService", function(id, text) {
		 
		   alert("Event received, id="+id+", text="+text);
		   alert("print job");
		   var viewer = window.contentViewer
		   var tabsLength = viewer.mainTabContainer.getChildren().length;
		   var tabs = viewer.mainTabContainer.getChildren();
		   alert(tabs);

			for(var i=0;i<tabsLength;i++){
						if(tabs[i].selected){
							alert("Tab no: "+i +"is selected");
							console.log("Tab no: "+i +"is selected");
							var viewerPane = tabs[i].contentViewerPane;
							var item = viewerPane.viewerItem.item;
							var docId = item.attributes.Id;
							var docName = item.name;
							var objectStore	= item.objectStoreName;
							
					     	var serviceParams = new Object();
							serviceParams.docId = docId;
							serviceParams.docName = docName;
							serviceParams.objectStore = objectStore;

							Request.invokePluginService("ViewOnePlugin","LogDocumentPrintService", {
										requestParams : serviceParams,
										requestCompleteCallback : function(response) {
											
										}
									});
							}
		}
			
		
		
		});*/
	
	
});
