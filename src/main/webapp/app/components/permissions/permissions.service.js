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

    PermissionsService.$inject = ['$http', 'Upload'];

    function PermissionsService($http, Upload) {
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

        /**
         * Call PermissionREST.java#getPermissionsAndRequestsForUser() and handle call backs
         */
        service.getPermissionsAndRequestsForUser = function() {
            return $http.get(API_URL + 'getPermissionsAndRequestsForUser').then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            })
        };

        /**
         * Call PermissionREST.java#requestPermissoin() and handle call backs.
         * @param stream the stream item to request the permission for
         * @param type permission type i.e. admin or write
         */
        service.requestPermission = function(stream, type) {
            return Upload.upload({
                url: API_URL + 'requestPermission',
                data: {
                    stream : angular.toJson(stream),
                    type : type
                }
            }).then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            })
        };

        /**
         * Calls PermissionREST.java#rejectPermission() and handle call backs
         * @param streamRequest the stream request to reject
         */
        service.rejectPermission = function(streamRequest) {
            return Upload.upload({
                url: API_URL + 'rejectPermission',
                data: {
                    streamRequest : angular.toJson(streamRequest)
                }
            }).then(function(response) {
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
