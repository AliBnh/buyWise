-- ==============================================================================
-- BuyWise PostgreSQL Flyway Migration V2: Seed Baseline Categories & Sellers
-- ==============================================================================

-- Seed Product Categories
INSERT INTO product_categories (name, slug, description, icon_name)
VALUES 
    ('Laptops & Computers', 'laptops-computers', 'Portable computers, ultrabooks, gaming laptops, and accessories', 'Laptop'),
    ('Major Appliances', 'major-appliances', 'Refrigerators, washing machines, dryers, and dishwashers', 'Refrigerator'),
    ('Smartphones & Tablets', 'smartphones-tablets', 'Mobile phones, tablets, and smart wearables', 'Smartphone'),
    ('TVs & Home Entertainment', 'tvs-home-entertainment', 'Smart televisions, soundbars, and home cinema systems', 'Tv')
ON CONFLICT (slug) DO NOTHING;

-- Seed Verified Regional & National Retailers
INSERT INTO sellers (name, website_url, reputation_score, return_policy_summary, is_verified)
VALUES
    ('ElectroMaroc', 'https://electromaroc.ma', 4.80, '14-day hassle-free return window with original packaging; 24-month manufacturer warranty handled in-country.', TRUE),
    ('TechHub Casablanca', 'https://techhub.ma', 4.50, '7-day replacement for dead-on-arrival; 12-month standard importer warranty.', TRUE),
    ('Cosmos Electro', 'https://cosmos.ma', 4.60, '15-day return policy; authorized warranty repair centers across all major Moroccan cities.', TRUE),
    ('Marjane Mall', 'https://marjanemall.ma', 4.70, 'Official distributor warranty; 14-day returns supported by local store drop-off.', TRUE);
