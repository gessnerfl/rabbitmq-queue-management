var module = angular.module('rmqmgmt', ['jsonFormatter']);
module.controller('main', function($scope, $http) {
	
	$scope.queuesFound = false;
	
	$scope.init = function(){
		$http.get('/queues').then(function(response) { 
	    	if(response.data !== undefined && response.data.length > 0){
	    		$scope.queues = response.data;
	    		$scope.queuesFound = true;
	    	}else{
	    		$scope.queues = undefined;
	    		$scope.queuesFound = false;
	    	}
	    });
	};
	$scope.init();
	
	$scope.isQueueSelected = function(q){
	    return $scope.selectedQueue !== undefined && $scope.selectedQueue.name === q.name;
	};
	
	$scope.selectQueue = function(q){
	    $scope.selectedQueue = q;
		if($scope.selectedQueue !== undefined){
			$scope.loadMessages();
		}else{
			$scope.clearMessages();
		}
	};
	
	$scope.loadMessages = function(){
		$http.get('/queues/'+$scope.selectedQueue.name+"/messages").then(function(response) { 
	    	if(response.data !== undefined && response.data.length > 0){
	    		$scope.renderMessages(response.data);
	    	}else{
	    		$scope.clearMessages();
	    	}
	    });
	};
	
	$scope.hasDlx = function(queue){
	    return queue.arguments !== undefined && queue.arguments["x-dead-letter-exchange"] !== undefined;
	};
    
    $scope.hasDlk = function(queue){
        return queue.arguments !== undefined && queue.arguments["x-dead-letter-routing-key"] !== undefined;
    };
	
	$scope.renderMessages = function(messages){
		$scope.messages = messages;
	};
	
	$scope.clearMessages = function(){
		$scope.messages = undefined;
	};
	
	$scope.openMoveModal = function(m){
		$scope.selectedMessage = m;
		$("#moveMessageModal > .modal").modal('show');
	};
	
	$scope.openDeleteModal = function(m){
		$scope.selectedMessage = m;
		$("#deleteMessageModal > .modal").modal('show');
	};
    
});