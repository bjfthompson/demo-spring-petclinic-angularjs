'use strict';

angular.module('upcomingVisits')
    .controller('UpcomingVisitsController', ['$http', function ($http) {
        var self = this;

        self.days = 7;
        self.query = '';
        self.visits = [];
        self.loading = false;

        self.setDays = function (days) {
            self.days = days;
            self.load();
        };

        self.search = function () {
            self.load();
        };

        self.clearSearch = function () {
            self.query = '';
            self.load();
        };

        self.load = function () {
            self.loading = true;
            var params = { days: self.days };
            if (self.query && self.query.trim()) {
                params.q = self.query.trim();
            }
            $http.get('api/visits/upcoming', { params: params }).then(function (resp) {
                self.visits = resp.data;
                self.loading = false;
            }, function () {
                self.visits = [];
                self.loading = false;
            });
        };

        self.load();
    }]);
