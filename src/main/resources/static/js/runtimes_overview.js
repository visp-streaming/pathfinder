var runtimePullInterval = null;

$(document).ready(function () {

    updateRuntimes();
    runtimePullInterval = window.setInterval(function () {
        updateRuntimes();
    }, GLOBAL_POLL_INTERVAL);
});


var showFallbackRuntimes = function () {
    $("#current_runtimes_rows").html("");
    $("#current_runtimes_unavailable").show();
    $("#current_runtimes_content").hide();
}

var updateRuntimes = function () {

    $.ajax({
        url: "/webfrontend/getRuntimeData"
    })
        .done(function (data) {

            if (data["instances"].length == 0 || data["instances"].length == 0) {
                showFallbackRuntimes();
                return;
            }


            var inHTML = "";

            $.each(data.instances, function (index, value) {
                var newItem = "<tr>" +
                    "                                    <th scope=\"row\">" + value.id + "</th>" +
                    "                                    <td>" + value.ip + "</td>" +
                    "                                    <td>" + value.port + "</td>" +
                    "                                    <td><button type=\"button\"" +
                    " class=\"btn btn-xs btn-primary\">Info</button> <button type=\"button\" class=\"btn " +
                    "btn-xs btn-danger\" onclick='removeRuntime(\"" + value.ip + ":" + value.port + "\")'>Remove</button></td>\n" +
                "                                </tr>";
                inHTML += newItem;
            });

            $("#current_runtimes_rows").html(inHTML);

            $("#current_runtimes_unavailable").hide();
            $("#current_runtimes_content").show();

        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showFallbackRuntimes();
            clearInterval(runtimePullInterval);
        });
};

var removeRuntime = function (endpoint) {
    $.ajax({
        type: "get", //send it through get method
        data: {
            endpoint: endpoint
        },
        url: "/communication/removeVispRuntime"
    })
        .done(function (data) {
            console.log("Successfully removed VISP runtime");
            updateRuntimes();
        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");

        });
}

var addRuntime = function () {
    $.ajax({
        type: "get", //send it through get method
        data: {
            endpoint: $("#visp_endpoint_to_add").val()
        },
        url: "/communication/addVispRuntime"
    })
        .done(function (data) {
            console.log("Successfully added VISP runtime");
            $("#visp_endpoint_to_add").val("");
            updateRuntimes();
        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");

        });
}