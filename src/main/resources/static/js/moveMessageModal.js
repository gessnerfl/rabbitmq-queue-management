angular.module('rmqmgmt').directive('moveMessageModal', function() {
    return {
        replace : false,
        restrict : 'A',
        templateUrl : '/partials/moveMessageModal.html',
        scope : {
            queue : '=queue',
            message : '=message',
            successCallback : '=successCallback'
        },
        link : function(scope, element, attr) {
            scope.modalElement = $(element).find(".modal");
            $(scope.modalElement).on('show.bs.modal', function(e) {
                scope.error = undefined;
                scope.exchanges = undefined;
                scope.targetExchange = undefined;
                scope.routingKeys = undefined;
                scope.targetRoutingKey = undefined;
                scope.loadExchanges();
            });
        },
        controller : [ '$scope', '$http', 'modalRestExecutor', function($scope, $http, modalRestExecutor) {
            
            $scope.loadExchanges = function(){
                $http.get('/exchanges').then(function(response) { 
                    if(response.data !== undefined && response.data.length > 0){
                        $scope.exchanges = response.data;
                    }
                });
            };
            $scope.loadExchanges();
            
            $scope.isExchangeSelectionDisabled = function(){
                return $scope.exchanges === undefined || $scope.exchanges.length == 0;
            };
            
            $scope.buildExchangeName = function(name){
                if(name !== undefined && name !== null && name !== ""){
                    return name;
                }
                return "(AMQP Default)";
            };
            
            $scope.onExchangeSelected = function(){
                if($scope.targetExchange !== undefined){
                    $http.get('/exchanges/'+$scope.targetExchange.name+"/routingKeys").then(function(response) { 
                        if(response.data !== undefined && response.data.length > 0){
                            $scope.routingKeys = response.data;
                        }
                    });
                }
            };
            
            $scope.isRoutingKeySelectionDisabled = function(){
                return $scope.targetExchange === undefined || $scope.routingKeys === undefined || $scope.routingKeys.length == 0;
            };

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
            
            $scope.isTargetDefined = function(){
                return $scope.targetExchange === undefined || $scope.targetRoutingKey === undefined;
            };

            $scope.move = function($event) {
                if (!($($event.currentTarget).hasClass('disabled'))){
                    var url = '/queues/' + $scope.queue.name + 
                                '/messages/move?checksum=' + $scope.message.checksum + 
                                '&targetExchange=' + $scope.targetExchange.name +
                                "&targetRoutingKey=" + $scope.targetRoutingKey;
                    modalRestExecutor('POST', url, $scope);
                }
            };

        } ]
    };
});