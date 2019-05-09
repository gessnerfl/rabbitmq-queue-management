module.controller('messages', function($scope, $http, $location, $route, errorHandler) {
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
	    $scope.enrichFirstMessageWithRequeueDetails(messages[0])
		$scope.messages = messages;
	};

	$scope.enrichFirstMessageWithRequeueDetails = function(message){
	    if(message.properties.headers !== undefined && message.properties.headers["x-death"] !== undefined){
	        var xdeath = message.properties.headers["x-death"][0];
	        var exchange = xdeath.exchange;
	        var routingKey = xdeath["routing-keys"] !== undefined && xdeath["routing-keys"].length > 0 ? xdeath["routing-keys"][0] : null;

	        if(!isEmptyString(exchange) && !isEmptyString(routingKey)){
	            message.requeueAllowed = true;
	            message.requeueExchange = exchange;
	            message.requeueRoutingKey = routingKey;
	        } else {
	            message.requeueAllowed = false;
	        }
	    } else {
            message.requeueAllowed = false;
        }
	}
	
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
	    var body = atob(message.body);
	    try {
           var json = JSON.parse(body.replace(/\s/g, ""));
           message.formattedBody = JSON.stringify(json, null , 2);
        } catch(e) {
           message.formattedBody = body;
        }
	}
	
	$scope.wasRequeued = function(m){
	    return m.properties.headers !== undefined && m.properties.headers["x-rmqmgmt-requeue-count"] > 0;
	}

	$scope.getRequeueCount = function(m){
	    return m.properties.headers["x-rmqmgmt-requeue-count"];
	}

	$scope.wasMoved = function(m){
	    return m.properties.headers !== undefined && m.properties.headers["x-rmqmgmt-move-count"] > 0;
	}

	$scope.getMoveCount = function(m){
	    return m.properties.headers["x-rmqmgmt-move-count"];
	}

	$scope.openRequeueModal = function(m){
	    errorHandler.clear();
		$scope.selectedMessage = m;
		$("#requeueMessageModal > .modal").modal('show');
	};

	$scope.openMoveModal = function(m){
	    errorHandler.clear();
		$scope.selectedMessage = m;
		$("#moveMessageModal > .modal").modal('show');
	};
	
	$scope.openDeleteModal = function(m){
	    errorHandler.clear();
		$scope.selectedMessage = m;
		$("#deleteMessageModal > .modal").modal('show');
	};

	$scope.openRequeueAllModal = function(m){
	    errorHandler.clear();
		$scope.selectedMessage = m;
		$("#requeueAllMessagesModal > .modal").modal('show');
	};

	$scope.openMoveAllModal = function(){
	    errorHandler.clear();
		$("#moveAllMessagesModal > .modal").modal('show');
	};

	$scope.openDeleteAllModal = function(){
	    errorHandler.clear();
		$("#deleteAllMessagesModal > .modal").modal('show');
	};
	$scope.init();
    
});