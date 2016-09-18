angular.module('rmqmgmt').factory("modalRestExecutor", ["$http", function($http){
    return function(method, url, modalScope){
        var onSuccess = function(response) {
            if (modalScope.successCallback !== undefined) {
                modalScope.successCallback();
            }
            $(modalScope.modalElement).modal('hide');
        };
        var onError = function(response) {
            if(response.data !== undefined && response.data.message !== undefined){
                modalScope.error = response.data.message;
            }else{
                modalScope.error = "An unexpected error occurred";
            }
        }
        $http({
            method : method,
            url : url
        }).then(onSuccess, onError);
    };
    
}]);