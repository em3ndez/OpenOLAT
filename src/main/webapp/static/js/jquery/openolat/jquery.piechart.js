(function ( $ ) {
	"use strict";
	$.fn.ooPieChart = function(options) {
		var settings = $.extend({
			w: 600,	// Width of the circle
			h: 600,	// Height of the circle
			margin: 20,
			layer: 0,
			colors: [],
			values: []
		}, options);

		try {
			pieChart(this, settings);
		} catch(e) {
			if(window.console) console.log(e);
		}
		
		function pieChart($obj, cfg) {
			// set the dimensions and margins of the graph
			var width = cfg.w;
			var height = cfg.h;
			var margin = cfg.margin;
			var layer = cfg.layer;
			var data = settings.values;
			var colorRange = settings.colors;
			
			// The radius of the pieplot is half the width or half the height (smallest one). I subtract a bit of margin.
			var radius = Math.min(width, height) / 2 - margin
			var innerRadius = 0;
			if(layer > 0) {
				innerRadius = Math.max(10, radius - layer);
			}
			
			// append the svg object to the div called 'my_dataviz'
			var id = $obj.attr('id');
			var svg = d3.select("#" + id)
			  .append("svg")
			    .attr("width", width)
			    .attr("height", height)
			  .append("g")
			    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

			// set the color scale
			var color = d3.scaleOrdinal()
			  .domain(data)
			  .range(colorRange);
			
			// Compute the position of each group on the pie:
			var pie = d3.pie()
			  .startAngle(0)
			  .sort(function (a, b) {
          			return a.key.localeCompare(b.key);
          	   })
			  .value(function(d) {return d.value; });
			var data_ready = pie(d3.entries(data));
			
			// Build the pie chart: Basically, each part of the pie is a path that we build using the arc function.
			svg
			  .selectAll('whatever')
			  .data(data_ready)
			  .enter()
			  .append('path')
			  .attr('d', d3.arc()
			    .innerRadius(innerRadius)         // This is the size of the donut hole
			    .outerRadius(radius)
			  )
			  .attr('fill', function(d){ return(color(d.data.key)) })
			  .attr("stroke", "black")
			  .style("stroke-width", "0px");
		}
	}
}( jQuery ));