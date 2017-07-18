var operatorPullInterval = null;

var showSplitOperatorInfoFallback = function() {
    $("#operatorDetailsContent").hide();
    $("#operatorDetailsContentFallback").show();
}

var currentDetailViewOperatorId = null;

var updateOperatorView = function() {
    // called from reload button in view

    if(currentDetailViewOperatorId == null) {
        return;
    } else {
        showSplitOperatorInfo(currentDetailViewOperatorId);
    }

};

var colorCircuitStatus = function(value) {
    if(value == "OPEN") {
        return "<span class=\"label label-danger\">OPEN</span>";
    } else if(value == "CLOSED") {
        return "<span class=\"label label-success\">CLOSED</span>";
    } else {
        return "<span class=\"label label-default\">" + value + "</span>";
    }
}

var showSplitOperatorInfo = function(operatorId) {
    currentDetailViewOperatorId = operatorId;
    console.log("operatorId: " + operatorId);
    $.ajax({
        type: "get", //send it through get method
        data: {
            operatorId: operatorId
        },
        url: "/webfrontend/getSplitOperatorDetails"
    })
        .done(function (data) {

            if (!data) {
                showSplitOperatorInfoFallback();
                return;
            }

            var inHTML = "";

            $.each(data.circuitBreakerStatus, function (index, value) {
                var newItem = "<tr>" +
                    "                                    <th scope=\"row\">" + index + "</th>" +
                    "                                    <td>" + colorCircuitStatus(value) + "</td>" +
                    "                                    <td><button type=\"button\"" +
                    " class=\"btn btn-xs btn-primary\">Info</button> <button type=\"button\" class=\"btn " +
                    "btn-xs btn-danger\" onclick='todo(\"" + value + "\")'>Todo</button></td>\n" +
                    "                                </tr>";
                inHTML += newItem;
            });



            $("#operator_details_content_table").html(inHTML);

            $("#operatorDetailsContentFallback").hide();
            $("#operatorDetailsContent").show();

        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showSplitOperatorInfoFallback();
        });
}

$(document).ready(function () {
    showFallbackOperators();
    updateOperators();
    operatorPullInterval = window.setInterval(function () {
        updateOperators();
    }, GLOBAL_POLL_INTERVAL);
});


var showFallbackOperators = function () {
    $("#operators_content_fallback").show();
    $("#operators_content").hide();
}

var updateOperators = function () {

    $.ajax({
        url: "/webfrontend/getOperators"
    })
        .done(function (data) {
            if (totalVispInstances === 0 || data.operators.length === 0) {
                // topology is empty
                showFallbackOperators();
                return;
            } else {
                var htmlRowsOperators = "";

                var labelWorking = "<span class=\"label label-success\">working</span>";
                var labelFailed = "<span class=\"label label-danger\">failed</span>";

                $.each(data.operators, function (index, value) {
                    var label = "";
                    if(value.operatorStatus == "working") {
                        label = labelWorking;
                    } else {
                        label = labelFailed;
                    }
                    var newItem = "<tr>" +
                        "                                    <th scope=\"row\">" + value.id + "</th>" +
                        "                                    <td>" + value.concreteLocation + "</td>" +
                        "                                    <td><span class=\"label label-default\">" + value.subclass + "</span></td>" +
                        "                                    <td>" + label + "</td>" +
                        "                                    <td><button type=\"button\" class=\"btn " +
                        "btn-xs btn-primary\" onclick='showOperatorInfo(\"" + value.id + "\")'>Info</button></td>\n" +
                        "                                </tr>";
                    htmlRowsOperators += newItem;
                });

                $("#operators_rows").html(htmlRowsOperators);


                var htmlRowsSplitOperators = "";

                $.each(data.splitOperators, function (index, value) {

                    var newItem = "<tr>" +
                        "                                    <th scope=\"row\">" + value.id + "</th>" +
                        "                                    <td>" + value.activePath + "</td>" +
                        "                                    <td>" + value.totalPaths + "</td>" +
                        "                                    <td>" + value.failedPaths + "</td>" +
                        "                                    <td>" + value.availablePaths + "</td>" +
                        "                                    <td><button type=\"button\" class=\"btn " +
                        "btn-xs btn-primary\" onclick='showSplitOperatorInfo(\"" + value.id + "\")'>Info</button></td>\n" +
                        "                                </tr>";
                    htmlRowsSplitOperators += newItem;
                });

                $("#split_operators_rows").html(htmlRowsSplitOperators);


                $("#operators_content_fallback").hide();
                $("#operators_content").show();
            }

        })
        .fail(function () {
            console.log("Could not reach pathFinder backend");
            showFallbackOperators();
            clearInterval(operatorPullInterval);
        });
};

