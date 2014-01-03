from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^historical_data/', include('historical_data.urls', namespace="historical_data")),
    url(r'^admin/', include(admin.site.urls)),
)