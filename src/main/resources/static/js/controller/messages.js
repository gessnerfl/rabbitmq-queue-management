module.controller('messages', function($scope, $http, $location, $route) {
	$scope.init = function(){
	    var params = $route.current.params;
	    $scope.queue = params.queue;
	    $scope.vhost = params.vhost;
	    if(isEmptyString($scope.vhost) || isEmptyString($scope.queue)){
	        window.location = "/";
	    }

		$scope.loadMessages();
	};
	
	function isEmptyString(str){
	    return str === undefined || str === null || str.trim().length === 0;
	}
	
	$scope.loadMessages = function(){
		$http.get('/api/messages?vhost=' + encodeURIComponent($scope.vhost) + '&queue=' + encodeURIComponent($scope.queue)).then(function(response) {
	    	if(response.data !== undefined && response.data.length > 0){
	    		$scope.renderMessages(response.data);
	    	}else{
	    		$scope.clearMessages();
	    	}
	    });
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
	    var body = btoa(message.body);
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