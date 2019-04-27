var module = angular.module('rmqmgmt', ['jsonFormatter']);
module.controller('main', function($scope, $http, $location) {
	
	$scope.init = function(){
	    $scope.queueName = getParameterByName("qname");
	    $scope.vhost = getParameterByName("vhost");
	    if(isEmptyString($scope.vhost) || isEmptyString($scope.queueName)){
	        window.location = "/";
	    }

		$scope.loadMessages();
	};
	
	function getParameterByName(name) {
	    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
	    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
	}
	
	function isEmptyString(str){
	    return str === undefined || str === null || str.trim().length === 0;
	}
	
	$scope.loadMessages = function(){
		$http.get('/api/messages?vhost=' + encodeURIComponent($scope.vhost) + '&queue=' + encodeURIComponent($scope.queueName)).then(function(response) {
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
           var json = JSON.parse(body.replace(/\s/g, ""));
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
	$scope.init();
    
});