angular.module('rmqmgmt').factory("ajaxInterceptor", ["$q", "errorHandler", function($q, errorHandler) {
        return {
            'response': function(response) {
                return response;
            },

           'responseError': function(response) {
                errorHandler.show(response.data);
                return $q.reject(response);
           }
        };
     }
]);