from django.http import HttpResponse
from django.shortcuts import render, get_object_or_404

from historical_data.models import PositionTag

def index(request):
    tags = PositionTag.objects.all()
    context = {
        'tags': tags,
    }
    return render(request, 'historical_data/index.html', context)

def tag(request, tag_id):
    tag = get_object_or_404(PositionTag, pk=tag_id)
    return HttpResponse(str(tag))
