var module = angular.module('rmqmgmt', ['ngRoute', 'jsonFormatter']);

module.config(['$routeProvider', '$locationProvider', '$httpProvider',
  function($routeProvider, $locationProvider, $httpProvider){
    $locationProvider.html5Mode(true);
    $locationProvider.hashPrefix('!');

    $httpProvider.interceptors.push('ajaxInterceptor');

    $routeProvider
      .when('/queues', {
        templateUrl: '/partials/controller/queues.tpl.html',
        controller : 'queues'
      })
      .when('/messages', {
        templateUrl: '/partials/controller/messages.tpl.html',
        controller : 'messages'
      })
      .otherwise({
        redirectTo: '/queues'
      })
  }
]);