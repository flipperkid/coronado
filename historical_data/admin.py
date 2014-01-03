from django.contrib import admin
from historical_data.models import Position, PositionTag

admin.site.register(Position)
admin.site.register(PositionTag)
