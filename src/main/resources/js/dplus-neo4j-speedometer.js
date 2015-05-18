    google.load("visualization", "1", {
      packages: ["gauge"]
    });

    function createAndDrawChart(chartDivId, neo4jUrl, cypher, refreshIntervalInMillis, options) {
      var cypherClean = cypher.replace(/&lt;/g, "<").replace(/&gt;/g, ">");
    	
      var chart = new google.visualization.Gauge(document.getElementById(chartDivId));
      var data = new google.visualization.DataTable();
      var timer = null;
      if (refreshIntervalInMillis > 0) {
        timer = setInterval(function() {
          drawChart(chart, data, neo4jUrl, cypherClean, timer, options);
        }, refreshIntervalInMillis);
      }
      drawChart(chart, data, neo4jUrl, cypherClean, timer, options);
    }

    function drawChart(chart, data, neo4jUrl, cypher, timer, options) {
        $.ajax({
        	  type: "POST",
        	  url: neo4jUrl + "/db/data/cypher",
//        	  headers: {
//        		  "Authorization": "Basic " + btoa(USERNAME + ":" + PASSWORD)
//        	  },
        	  data: {query: cypher}
        })
          .done(function(n4jdata) {
            drawChartOnQuerySuccess(chart, data, neo4jUrl, cypher, timer, options, n4jdata);
          })
          .fail(function(xhr, textStatus, errorThrown) {
//            clearInterval(timer);
//            console.warn(
//	            "Considering the error above, the auto-refresh has been deactivated."
//            );
        	  console.warn("Could not update the chart because an error occured while executing POST request on "+neo4jUrl + "/db/data/cypher");
            drawChartOnQueryError(chart);
          });
    }

    function drawChartOnQueryError(chart) {
      var data = google.visualization.arrayToDataTable([
        ['Label', 'Value'],
        ['!', 0]
      ]);
      chart.draw(data, {});
    }

    function drawChartOnQuerySuccess(chart, data, neo4jUrl, cypher, timer, options, n4jdata) {
      var idxLabel = n4jdata.columns.indexOf("label");
      var idxValue = n4jdata.columns.indexOf("value");
      var idxMin = n4jdata.columns.indexOf("min");
      var idxMax = n4jdata.columns.indexOf("max");
      var idxRedFrom = n4jdata.columns.indexOf("redFrom");
      var idxRedTo = n4jdata.columns.indexOf("redTo");
      var idxGreenFrom = n4jdata.columns.indexOf("greenFrom");
      var idxGreenTo = n4jdata.columns.indexOf("greenTo");
      var idxYellowFrom = n4jdata.columns.indexOf("yellowFrom");
      var idxYellowTo = n4jdata.columns.indexOf("yellowTo");
      var idxMinorTicks = n4jdata.columns.indexOf("minorTicks");
      var idxMajorTicks = n4jdata.columns.indexOf("majorTicks");

      // validate n4jdata
      if (idxLabel == -1) {
        console.warn(
          "The Neo4j query (Cypher) does not specify any 'label' column!\n" + "Neo4j address = " +
          neo4jUrl + "\n" + "Cypher = " + cypher
        );
      }
      if (idxValue == -1) {
        console.warn(
          "The Neo4j query (Cypher) does not specify any 'value' column!\n" + "Neo4j address = " +
          neo4jUrl + "\n" + "Cypher = " + cypher
        );
      }
      if (idxLabel == -1 && idxValue == -1) {
        clearInterval(timer);
        console.warn(
          "Considering the warning above, the auto-refresh has been deactivated."
        );
      }

      // prepare/update data
      if (data.getNumberOfColumns() == 0) {
        data.addColumn('string', 'Label');
        data.addColumn('number', 'Value');
        $.each(n4jdata.data, function(key, record) {
          var newLabel = idxLabel == -1 ? "" : record[idxLabel];
          var newValue = idxValue == -1 ? 0 : record[idxValue];
          data.addRows([
            [newLabel, newValue]
          ]);
        });
      } else {
        $.each(n4jdata.data, function(key, record) {
          var newLabel = idxLabel == -1 ? "" : record[idxLabel];
          var newValue = idxValue == -1 ? 0 : record[idxValue];
          if (data.getValue(key, 0) != newLabel) data.setValue(key, 0, newLabel);
          if (data.getValue(key, 1) != newValue) data.setValue(key, 1, newValue);
        });
      }

      // determine min of mins and max of max
      var minOfMin = Number.POSITIVE_INFINITY,
        maxOfMax = Number.NEGATIVE_INFINITY;
      $.each(n4jdata.data, function(key, record) {
        if (idxMin != -1 && record[idxMin] < minOfMin) minOfMin = record[idxMin];
        if (idxMax != -1 && record[idxMax] > maxOfMax) maxOfMax = record[idxMax];
      });

      // prepare/update options
      var lastRecord = n4jdata.data.pop();
      if (idxMin != -1) options.min = minOfMin;
      if (idxMax != -1) options.max = maxOfMax;
      if (idxRedFrom != -1) options.redFrom = lastRecord[idxRedFrom];
      if (idxRedTo != -1) options.redTo = lastRecord[idxRedTo];
      if (idxGreenFrom != -1) options.greenFrom = lastRecord[idxGreenFrom];
      if (idxGreenTo != -1) options.greenTo = lastRecord[idxGreenTo];
      if (idxYellowFrom != -1) options.yellowFrom = lastRecord[idxYellowFrom];
      if (idxYellowTo != -1) options.yellowTo = lastRecord[idxYellowTo];
      if (idxMinorTicks != -1) options.minorTicks = lastRecord[idxMinorTicks];
      //if (idxMajorTicks != -1) options.majorTicks = lastRecord[idxMajorTicks];
      //options.majorTicks = ["1", "", "3", "", "4"];
      //console.log(options);

      // draw chart
      chart.draw(data, options);
    }
