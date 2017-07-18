var topologyPullInterval = null;
var cachedDotContent = false;

$(document).ready(function () {
    showFallbackTopology();
    updateTopology();
    topologyPullInterval = window.setInterval(function () {
        updateTopology();
    }, GLOBAL_POLL_INTERVAL);

});


var renderTopology = function(dotContent) {
    if(dotContent) {
        var image = Viz(dotContent, { format: "svg" });
        document.getElementById("topologyFigure").innerHTML = image;
    }
}

var showFallbackTopology = function () {
    $("#topology_content_fallback").show();
    $("#topology_content").hide();
}

var updateTopology = function () {

    $.ajax({
        url: "/webfrontend/getTopology"
    })
        .done(function (data) {
            if (totalVispInstances === 0 || data["topology"].trim().length == 0) {
                // topology is empty
                showFallbackTopology();
                return;
            } else {
                var topologyFileContent = (window.atob(data.topology)).replace(/(?: )/g, '&nbsp;').replace(/(?:\r\n|\r|\n)/g, '<br />');

                $("#topology_file_content").html(topologyFileContent);
                $("#topology_content_fallback").hide();
                $("#topology_content").show();

                if(data["dotContent"] != cachedDotContent) {
                    cachedDotContent = data["dotContent"];
                    renderTopology(data["dotContent"]);
                }

            }

        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showFallbackTopology();
            clearInterval(topologyPullInterval);
        });
};

