/**
 * Created by Jagrut on 10-07-2017.
 */
(function(){
    "use strict";
    /**
     * Service method to send requests to backend for all events related to permissions.
     */
    angular
        .module('plagUiApp')
        .factory('PermissionsService', PermissionsService);

    PermissionsService.$inject = ['$http'];

    function PermissionsService($http) {
        const API_URL = '/api/plagchain/';
        var service = {};

        /**
         * Call PermissionREST.java#getPermissionsForUser() and handle call backs
         */
        service.getPermissionsForUser = function() {
            return $http.get(API_URL + 'getPermissionsForUser').then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            })
        };

        return service;
    }
})();
