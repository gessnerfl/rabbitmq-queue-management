var module = angular.module('rmqmgmt', ['jsonFormatter']);
module.controller('main', function($scope, $http, $location) {
	
	$scope.queuesFound = false;
	
	$scope.init = function(){
	    $scope.brokerName = getParameterByName("selected");
	    if(isEmptyString($scope.brokerName)){
	        window.location = "/";
	    }
		$http.get('/api/'+$scope.brokerName+'/queues').then(function(response) { 
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
	
	function getParameterByName(name) {
	    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
	    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
	}
	
	function isEmptyString(str){
	    return str === undefined || str === null || str.trim().length === 0;
	}
	
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
		$http.get('/api/'+$scope.brokerName+'/queues/'+$scope.selectedQueue.name+"/messages").then(function(response) {
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
	
	$scope.renderBody = function(message){
	    if(message.formattedBody === undefined){
	        $scope.formatBody(message)
	    }
	    return message.formattedBody;
	};

	$scope.formatBody = function(message){
	    var body = Base64.decode(message.body);
	    try {
           var json = JSON.parse(body);
           message.formattedBody = JSON.stringify(json, null , 2);
        } catch(e) {
           message.formattedBody = body;
        }
	}
	
	$scope.openMoveModal = function(m){
		$scope.selectedMessage = m;
		$("#moveMessageModal > .modal").modal('show');
	};
	
	$scope.openDeleteModal = function(m){
		$scope.selectedMessage = m;
		$("#deleteMessageModal > .modal").modal('show');
	};
    
});