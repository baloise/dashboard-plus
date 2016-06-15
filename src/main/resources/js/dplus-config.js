AJS.toInit(function() {
  var baseUrl = AJS.$("meta[name='confluence-base-url']").attr("content");
    
  function populateForm() {
    AJS.$.ajax({
      url: baseUrl + "/rest/dashboard-plus/1.0/PluginConfig",
      dataType: "json",
      success: function(config) {
        AJS.$("#globalJiraEvalSpec").attr("value", config.globalJiraEvalSpec);
      }
    });
  }
  function updateConfig() {
    AJS.$.ajax({
      url: baseUrl + "/rest/dashboard-plus/1.0/PluginConfig",
      type: "PUT",
      contentType: "application/json",
      data: JSON.stringify({
    	  globalJiraEvalSpec: AJS.$("#globalJiraEvalSpec").attr("value"),
      }),
      processData: false,
      error: function(xhr, textStatus, errorThrown) {
    	  AJS.$("#globalJiraEvalSpec").data("changed",true);
    	  console.log(xhr);
    	  console.log(textStatus);
    	  console.log(errorThrown);
      }
    });
  }  
  populateForm();

  AJS.$("#dplus-config").submit(function(e) {
    e.preventDefault();
    updateConfig();
  });
});