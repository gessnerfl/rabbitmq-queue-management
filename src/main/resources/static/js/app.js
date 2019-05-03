var module = angular.module('rmqmgmt', ['ngRoute', 'jsonFormatter']);
module.config(['$routeProvider', '$locationProvider',
  function($routeProvider, $locationProvider){
    $locationProvider.html5Mode(true);
    $locationProvider.hashPrefix('!');

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