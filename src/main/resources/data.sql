INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'WiFi') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'Kitchen') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'Parking') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'Pool') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'AC') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'Heating') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'Washer') ON CONFLICT (name) DO NOTHING;
INSERT INTO amenities (id, name) VALUES (nextval('amenity_seq'), 'TV') ON CONFLICT (name) DO NOTHING;