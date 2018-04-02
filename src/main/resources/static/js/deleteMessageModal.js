angular.module('rmqmgmt').directive('deleteMessageModal', function() {
    return {
        replace : false,
        restrict : 'A',
        templateUrl : '/partials/deleteMessageModal.html',
        scope : {
            broker : '=broker',
            queue : '=queue',
            message : '=message',
            successCallback : '=successCallback'
        },
        link : function(scope, element, attr) {
            scope.modalElement = $(element).find(".modal");
            $(scope.modalElement).on('show.bs.modal', function(e) {
                scope.error = undefined;
            });
        },
        controller : [ '$scope', 'modalRestExecutor', function($scope, modalRestExecutor) {

            $scope.getQueueName = function() {
                if ($scope.queue !== undefined) {
                    return $scope.queue.name;
                }
                return "<queue missing>"
            };

            $scope.getChecksum = function() {
                if ($scope.message !== undefined) {
                    return $scope.message.checksum;
                }
                return "<message missing>"
            };

            $scope.onConfirm = function() {
                var url = '/api/' + $scope.broker + '/queues/' + $scope.queue.name + '/messages?checksum=' + window.encodeURIComponent($scope.message.checksum);
                modalRestExecutor('DELETE', url, $scope);
            };

        } ]
    };
});