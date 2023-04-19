package com.dynatrace.diagnostics.uemload.scenarios;

/**
 * @author Rafal.Psciuk
 *
 */

public class VisitsModel {
	private final int bounce;
	private final int search;
	private final int almost;
	private final int convert;
	private final int b2b;
	private final int seo;
	private final int specialOffers;
	private final int imageGallery;
	private final int magentoShort;
	private final int magentoLong;
	private final int wandererShort;
	private final int wandererLong;
	private final int wandererConvertShort;
	private final int wandererConvertLong;
	private final int amp;
	
	private VisitsModel(VisitsBuilder builder){
		this.bounce = builder.getBounce();
		this.search = builder.getSearch();
		this.almost = builder.getAlmost();
		this.convert = builder.getConvert();
		this.b2b = builder.getB2b();
		this.seo = builder.getSeo();
		this.specialOffers = builder.getSpecialOffers();
		this.imageGallery = builder.getImageGallery();
		this.magentoShort = builder.getMagentoShort();
		this.magentoLong = builder.getMagentoLong();
		this.wandererShort = builder.getWandererShort();
		this.wandererLong = builder.getWandererLong();
		this.wandererConvertShort = builder.getWandererConvertShort();
		this.wandererConvertLong = builder.getWandererConvertLong();
		this.amp = builder.getAmp();
	}
	
	public int getBounce() {
		return bounce;
	}

	public int getSearch() {
		return search;
	}

	public int getAlmost() {
		return almost;
	}

	public int getConvert() {
		return convert;
	}

	public int getB2b() {
		return b2b;
	}

	public int getSeo() {
		return seo;
	}

	public int getSpecialOffers() {
		return specialOffers;
	}

	public int getImageGallery() {
		return imageGallery;
	}

	public int getMagentoShort() {
		return magentoShort;
	}
	
	public int getMagentoLong() {
		return magentoLong;
	}
	
	public int getWandererShort() {
		return wandererShort;
	}
	
	public int getWandererLong() {
		return wandererLong;
	}
	
	public int getWandererConvertShort() {
		return wandererConvertShort;
	}
	
	public int getWandererConvertLong() {
		return wandererConvertLong;
	}
	
	public int getAmp(){
		return amp;
	}

	public static class VisitsBuilder {
		private int bounce;
		private int search;
		private int almost;
		private int convert;
		private int b2b;
		private int seo;
		private int specialOffers;
		private int imageGallery;
		private int magentoShort;
		private int magentoLong;
		private int wandererShort;
		private int wandererLong;
		private int wandererConvertShort;
		private int wandererConvertLong;
		private int amp;
		
		public VisitsModel build() {
			return new VisitsModel(this);
		}
		
		public VisitsBuilder setDefaults(){
			setSeo(10).setSpecialOffers(3).setImageGallery(10).setMagentoShort(10);
			return this;
		}
		
		public VisitsBuilder setBounce(int bounce) {
			this.bounce = bounce;
			return this;
		}
		
		public VisitsBuilder  setSearch(int search) {
			this.search = search;
			return this;
		}
		
		public VisitsBuilder  setAlmost(int almost) {
			this.almost = almost;
			return this;
		}
		
		public VisitsBuilder  setConvert(int convert) {
			this.convert = convert;
			return this;
		}
		
		public VisitsBuilder  setB2b(int b2b) {
			this.b2b = b2b;
			return this;
		}
		
		public VisitsBuilder  setSeo(int seo) {
			this.seo = seo;
			return this;
		}
		
		public VisitsBuilder  setSpecialOffers(int specialOffers) {
			this.specialOffers = specialOffers;
			return this;
		}
		
		public VisitsBuilder  setImageGallery(int imageGallery) {
			this.imageGallery = imageGallery;
			return this;
		}
		
		public VisitsBuilder  setMagentoShort(int magentoShort) {
			this.magentoShort = magentoShort;
			return this;
		}
		
		public VisitsBuilder  setMagentoLong(int magentoLong) {
			this.magentoLong = magentoLong;
			return this;
		}
		
		public VisitsBuilder setWandererShort(int wandererShort) {
			this.wandererShort = wandererShort;
			return this;
		}
		
		public VisitsBuilder setWandererLong(int wandererLong) {
			this.wandererLong = wandererLong;
			return this;
		}
		
		public VisitsBuilder setWandererConvertShort(int wandererConvertShort) {
			this.wandererConvertShort = wandererConvertShort;
			return this;
		}
		
		public VisitsBuilder setWandererConvertLong(int wandererConvertLong) {
			this.wandererConvertLong = wandererConvertLong;
			return this;
		}
		
		public VisitsBuilder setAmp(int amp) {
			this.amp = amp;
			return this;
		}

		public int getBounce() {
			return bounce;
		}

		public int getSearch() {
			return search;
		}

		public int getAlmost() {
			return almost;
		}

		public int getConvert() {
			return convert;
		}

		public int getB2b() {
			return b2b;
		}

		public int getSeo() {
			return seo;
		}

		public int getSpecialOffers() {
			return specialOffers;
		}

		public int getImageGallery() {
			return imageGallery;
		}

		public int getMagentoShort() {
			return magentoShort;
		}
		
		public int getMagentoLong() {
			return magentoLong;
		}
		
		public int getWandererShort() {
			return wandererShort;
		}
		
		public int getWandererLong() {
			return wandererLong;
		}
		
		public int getWandererConvertShort() {
			return wandererConvertShort;
		}
		
		public int getWandererConvertLong() {
			return wandererConvertLong;
		}
		
		public int getAmp() {
			return amp;
		}
	}
}
