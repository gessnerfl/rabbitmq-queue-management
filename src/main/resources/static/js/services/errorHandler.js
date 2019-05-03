'use strict';
angular.module('rmqmgmt').factory("errorHandler", [function() {
    function renderErrorResponse(response) {
        if (response.message !== undefined) {
            showMessage(response.message);
        } else {
            showMessage('Unexpected Error occured');
        }
    }

    function showMessage(errorText) {
        var messageDiv = getMessageDiv();
        var alertDiv;
        var messageSpan;

        if (messageDiv != undefined) {
            clearMessages();
            alertDiv = $("<div>").addClass("alert")
                                 .addClass("alert-danger")
                                 .addClass("alert-dismissible")
                                 .addClass("fade")
                                 .addClass("show")
                                 .appendTo(messageDiv);
            alertDiv.append("<i class='oi oi-warning' aria-hidden='true'></i>")
            messageSpan = $("<span>").appendTo(alertDiv)
            messageSpan.text(" "+errorText)
        } else {
            alert(errorText);
        }
    }

    function clearMessages() {
        $(".message-panel").children().remove();
    }

    function getMessageDiv() {
        var modalDiv = $(".modal:visible").find(".message-panel");
        if (modalDiv.length != 0) {
            return modalDiv.first();
        } else {
            return $('.message-panel').first();
        }
    }

    return {
        show : function(response) {
            clearMessages();
            renderErrorResponse(response);
        },
        clear : function() {
            clearMessages();
        }
    };
}
]);
