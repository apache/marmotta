/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created with IntelliJ IDEA.
 * User: wastl
 * Date: 28.11.13
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */
var loggingApp = angular.module('logging', []);

loggingApp.controller('LoggingController', function ($scope, $http) {
    $scope.levels = ['OFF', 'ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'];


    $http.get(url + 'logging/modules').success(function(data) {
        $scope.modules = data;

        $scope.$watch('modules', function(newValue, oldValue) {
            if(oldValue != newValue) {
                $scope.needsModulesSave = true;
            }
        }, true);
    });

    $http.get(url + 'logging/appenders').success(function(data) {
        $scope.appenders = data;

        $scope.$watch('appenders', function(newValue, oldValue) {
            if(oldValue != newValue) {
                $scope.needsAppendersSave = true;
            }
        }, true);
    });

    $scope.removeModuleAppender = function(module,appender_id) {
        var i = module.appenders.indexOf(appender_id);
        if(i >= 0) {
            module.appenders.splice(i,1);
        }
    };

    $scope.addModuleAppender = function(module,appender_id) {
        var i = module.appenders.indexOf(appender_id);
        if(i < 0) {
            module.appenders.push(appender_id);
        }
    };

    $scope.getUnselectedModuleAppenders = function(module) {
        var result = [];
        for(idx in $scope.appenders) {
            if(module.appenders.indexOf($scope.appenders[idx].id) < 0) {
                result.push($scope.appenders[idx]);
            }
        }
        return result;
    };

    /**
     * Save all appenders in this scope back to the Marmotta Webservice
     */
    $scope.saveAppenders = function() {
        // $http.post takes the old model, so we use jQuery
        $.ajax({
            type:  "POST",
            url:   url + 'logging/appenders',
            data:  angular.toJson($scope.appenders),
            contentType: 'application/json'
        });

        //$http.post(url + 'logging/appenders', $scope.appenders);
        $scope.needsAppendersSave = false;
    }


    /**
     * Save all modules in this scope back to the Marmotta Webservice
     */
    $scope.saveModules = function() {
        // $http.post takes the old model, so we use jQuery
        $.ajax({
            type:  "POST",
            url:   url + 'logging/modules',
            data:  angular.toJson($scope.modules),
            contentType: 'application/json'
        });

        //$http.post(url + 'logging/modules', mods);
        $scope.needsModulesSave = false;
    }

    /*
     * Watch updates to the model and set a flag to enable save buttons in UI
     */
    $scope.needsModulesSave = false;
    $scope.needsAppendersSave = false;


});