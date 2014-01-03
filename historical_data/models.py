from __future__ import unicode_literals

from django.db import models

class Position(models.Model):
    shares = models.FloatField(blank=True, null=True)
    cost_basis = models.FloatField(blank=True, null=True)
    close_value = models.FloatField(blank=True, null=True)
    open_date = models.DateTimeField(blank=True, null=True)
    close_date = models.DateTimeField(blank=True, null=True)
    closed = models.NullBooleanField()
    symbol = models.CharField(max_length=255, blank=True)
    cusip = models.CharField(max_length=255, blank=True)
    description = models.CharField(max_length=255, blank=True)
    security_type = models.CharField(max_length=255, blank=True)
    def __unicode__(self):
        return self.symbol + ': ' + self.description

class PositionTag(models.Model):
    tag = models.CharField(max_length=255, blank=True)
    positions = models.ManyToManyField(Position, blank=True)
    def __unicode__(self):
        return self.tag

#class AccountHistoryResponse(models.Model):
#    activity = models.CharField(max_length=255, blank=True)
#    amount = models.FloatField(blank=True, null=True)
#    date = models.DateTimeField(blank=True, null=True)
#    cusip = models.CharField(max_length=255, blank=True)

#class AbstractResolution(models.Model):
#    dtype = models.CharField(max_length=10)
#    parent_cusip = models.CharField(max_length=255, blank=True)
#    split_ratio = models.FloatField(blank=True, null=True)

#class Bookkeeping(models.Model):
#    date = models.DateTimeField(blank=True, null=True)
#    symbol = models.CharField(max_length=255, blank=True)
#    cusip = models.CharField(max_length=255, blank=True)
#    description = models.CharField(max_length=255, blank=True)
#    amount = models.FloatField(blank=True, null=True)
#    quantity = models.FloatField(blank=True, null=True)
#    resolution = models.ForeignKey(AbstractResolution, blank=True, null=True)

#class HistorySequence(models.Model):
#    symbol = models.CharField(max_length=255, blank=True)
#    start_date = models.DateTimeField(blank=True, null=True)
#    end_date = models.DateTimeField(blank=True, null=True)

#class QuoteHistory(models.Model):
#    high = models.FloatField(blank=True, null=True)
#    low = models.FloatField(blank=True, null=True)
#    open = models.FloatField(blank=True, null=True)
#    close = models.FloatField(blank=True, null=True)
#    symbol = models.CharField(max_length=255, blank=True)
#    date = models.DateTimeField(blank=True, null=True)
