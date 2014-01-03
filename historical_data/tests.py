from django.test import TestCase

from historical_data.models import Position

class PositionMethodTests(TestCase):

    def test_position_string(self):
        pos = Position(symbol='AAPL', description='Shares of Apple.')
        self.assertEqual(str(pos), 'AAPL: Shares of Apple.')