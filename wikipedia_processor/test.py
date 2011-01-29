# -*- coding: utf-8 -*-

import unittest
from events_processor import Coords, Event, processYear
from page_parser import WikiPage

class TestCoords(unittest.TestCase):
    def setUp(self):
        pass

    def test_init_floats_minimal(self):
        c = Coords("39.617998|22.404998")
        self.assertAlmostEqual(39.617998, c.lat, places=4)
        self.assertAlmostEqual(22.404998, c.long, places=4)

    def test_init_floats_with_compass_points(self):
        c = Coords("2.35|N|99.1|E|")
        self.assertAlmostEqual(2.350000, c.lat, places=4)
        self.assertAlmostEqual(99.100000, c.long, places=4)

    def test_init_negative_longitude(self):
        c = Coords("45.85833|-84.87083|region:US-MI_type:isle|name=St. Helena Island")
        self.assertAlmostEqual(45.85833, c.lat, places=4)
        self.assertAlmostEqual(-84.87083, c.long, places=4)

    def test_init_negative_latitude(self):
        c = Coords("-84.87083|45.85833|region:US-MI_type:isle|name=St. Helena Island")
        self.assertAlmostEqual(45.85833, c.long, places=4)
        self.assertAlmostEqual(-84.87083, c.lat, places=4)

    def test_degrees_minutes_ints(self):
        c = Coords("66|34|N|023|51|E|type:country_region:FI|name=04 Lapland Province, Finland")
        self.assertAlmostEqual(66.566664, c.lat, places=4)
        self.assertAlmostEqual(23.849997, c.long, places=4)

    def test_degrees_minutes_ints_minimal(self):
        c = Coords("66|34|N|023|51|E|type:country_region:FI|name=04 Lapland Province, Finland")
        self.assertAlmostEqual(66.566666666, c.lat, places=4)
        self.assertAlmostEqual(23.85, c.long, places=4)

    def test_init_degrees_minutes_negative_longitude(self):
        c = Coords("33|0|N|86|40|W|type:adm2nd_dim:1000000|display=title")
        self.assertAlmostEqual(33.0, c.lat)
        self.assertAlmostEqual(-86.6666666666, c.long, places=4)

    def test_init_degrees_minutes_negative_latitude(self):
        c = Coords("49|21|S|70|16|E|region:FR-TF_type:landmark |name=FUSOV")
        self.assertAlmostEqual(-49.349999, c.lat, places=4)
        self.assertAlmostEqual(70.266666, c.long, places=4)

    def test_init_degrees_minutes_unexpected_space(self):
        c = Coords("48|37|S|68|44|E |type:isle_region:FR-TF |name=")
        self.assertAlmostEqual(-48.616666, c.lat, places=4)
        self.assertAlmostEqual(68.733333, c.long, places=4)

    def test_init_degrees_minutes_utf_8_char(self):
        c = Coords("48|37|S|68|44|E |type:isle_region:FR-TF |name=Îles Nuageuses")
        self.assertAlmostEqual(-48.616666, c.lat, places=4)
        self.assertAlmostEqual(68.733333, c.long, places=4)

    def test_init_degrees_minutes_seconds(self):
        c = Coords("35|33|32|S|138|52|48|E|region:AU-SA_type:river_source:dewiki|display=title")
        self.assertAlmostEqual(-35.55533333, c.lat, places=5)
        self.assertAlmostEqual(138.8746666666, c.long, places=5)

class TestEvents(unittest.TestCase):
    def test_init_standard(self):
        e = Event("* [[January 6]] &ndash; The last natural [[pyrenean ibex]] is found dead apparently killed by a falling tree.", 2010)
        self.assertEqual(2010, e.year)
        self.assertEqual(1, e.month)
        self.assertEqual(6, e.day)
        self.assertEqual('January 6 &ndash; The last natural pyrenean ibex is found dead apparently killed by a falling tree.', e.eventText)
        self.assertEqual(["January 6", "pyrenean ibex"], e.links)

    def test_init_range(self):
        e = Event("* [[January 3]]–[[January 10|10]] &ndash; [[Israel]] and [[Syria]] hold inconclusive peace talks.", 2010)
        self.assertEqual(2010, e.year)
        self.assertEqual(1, e.month)
        self.assertEqual(3, e.day)
        self.assertEqual("January 3–10 &ndash; Israel and Syria hold inconclusive peace talks.", e.eventText)
        self.assertEqual(["January 3", "January 10", "Israel", "Syria"], e.links)

    def test_init_alternate_link(self):
        e = Event("""* [[January 24|24]] &ndash; [[God's Army (revolutionary group)|God's Army]],
                  a [[Karen people|Karen]] militia group led by twins [[Johnny and Luther Htoo]],
                  takes 700 hostages at a [[Thailand|Thai]] hospital near the [[Myanmar|Burmese]] border.""", 2010)
        self.assertEqual(2010, e.year)
        self.assertEqual(1, e.month)
        self.assertEqual(24, e.day)
        self.assertEqual(["January 24", "God's Army (revolutionary group)", "Karen people", "Johnny and Luther Htoo", "Thailand", "Myanmar"], e.links)

    def test_init_problematic(self):
        e = Event("* [[February 2]] &ndash; The first issue of ''[[Human Events]]'' is published.", 2010)
        self.assertEqual(2010, e.year)
        self.assertEqual(2, e.month)
        self.assertEqual(2, e.day)
        self.assertEqual("February 2 &ndash; The first issue of ''Human Events'' is published.", e.eventText)


class TestProcessYear(unittest.TestCase):
    def test_processYear_nestedEvents(self):
        events = """* [[January 18]]
** Scientists identify a previously unknown [[Bacteria|bacterium]] as the cause of the mysterious [[Legionellosis|Legionnaires' disease]].
** [[Australia]]'s worst [[Granville railway disaster|railway disaster at Granville]], near [[Sydney]], leaves 83 people dead.
** [[SFR Yugoslavia]] Prime minister, [[Džemal Bijedić]], his wife and 6 others are killed in a plane crash in [[Bosnia and Herzegovina]]."""
        wp = WikiPage()
        wp.title, wp.text = "2004", events
        processYear(wp)
        for event in wp.events:
            self.assertEqual(1, event.month)
            self.assertEqual(18, event.day)
        self.assertEqual("Australia's worst railway disaster at Granville, near Sydney, leaves 83 people dead.", wp.events[2].eventText)

if __name__ == '__main__':
    unittest.main()
