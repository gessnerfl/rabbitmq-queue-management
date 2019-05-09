angular.module('rmqmgmt').directive('deleteAllMessagesModal', function() {
    return {
        replace : false,
        restrict : 'A',
        templateUrl : '/partials/directives/deleteAllMessagesModal.tpl.html',
        scope : {
            vhost : '=vhost',
            queue : '=queue',
            successCallback : '=successCallback'
        },
        link : function(scope, element, attr) {
            scope.modalElement = $(element).find(".modal");
            $(scope.modalElement).on('show.bs.modal', function(e) {
                scope.error = undefined;
            });
        },
        controller : [ '$scope', 'modalRestExecutor', function($scope, modalRestExecutor) {
            $scope.onConfirm = function() {
                var url = '/api/messages?vhost=' + window.encodeURIComponent($scope.vhost) + '&queue=' + window.encodeURIComponent($scope.queue);
                modalRestExecutor('DELETE', url, $scope);
            };

        } ]
    };
});