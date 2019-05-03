module.controller('queues', function($scope, $http, $location) {
	$scope.init = function(){
		$scope.loadQueues();
	};
	
	$scope.loadQueues = function(){
		$http.get('/api/queues').then(function(response) {
	    	if(response.data !== undefined && response.data.length > 0){
	    		$scope.renderQueues(response.data);
	    	}else{
	    		$scope.clearQueues();
	    	}
	    });
	};

	$scope.renderQueues = function(queues){
		$scope.queues = queues;
	};

	$scope.clearQueues = function(){
		$scope.queues = undefined;
	};

	$scope.isDeadLetterExchangeConfigured = function(queue){
	    return queue.arguments !== undefined && queue.arguments["x-dead-letter-exchange"] !== undefined;
	};

    $scope.isDeadLetterRoutingKeyConfigured = function(queue){
        return queue.arguments !== undefined && queue.arguments["x-dead-letter-routing-key"] !== undefined;
    };

	$scope.init();
    
});