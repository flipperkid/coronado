from django.conf.urls import patterns, url

from historical_data import views

urlpatterns = patterns('',
    url(r'^$', views.index, name='index'),
    url(r'^tags/(?P<tag_id>\d+)/$', views.tag, name='tag'),
)