package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

/**
 * @author Rafal.Psciuk
 *
 */
public class UtmReferer implements Referer {
	
	public enum UtmMedium {	
		CPC("cpc"),
		EMAIL("email");
		
		private final String mediumName;
		
		private UtmMedium(String s) {
			mediumName = s;
		}
		
		public String getMediumName() {
			return mediumName;
		}
	}
		
	private final UtmMedium utm_medium;
	private final RandomSet<UtmSource> source;
	private final RandomSet<String> utm_campaign;
	private final RandomSet<String> utm_term;
	private final RandomSet<String> utm_content;
			
	public static class Builder {
		private UtmMedium utm_medium;
		private RandomSet<UtmSource> source;
		private RandomSet<String> utm_campaign;
		private RandomSet<String> utm_term;
		private RandomSet<String> utm_content;
		
		public Builder setSource(RandomSet<UtmSource> source) {
			this.source = source;
			return this;
		}
				
		public Builder setMedium(UtmMedium m) { 
			utm_medium = m;
			return this;
		}
		
		public Builder setCampaign(RandomSet<String> s) { 
			utm_campaign = s;
			return this;
		}
		
		public Builder setTerm(RandomSet<String> s) {
			utm_term = s;
			return this;
		}
		
		public Builder setContent(RandomSet<String> s) {
			utm_content = s;
			return this;
		}
		
		public UtmReferer build() {
			if(source == null || source.isEmpty()) {
				throw new IllegalArgumentException("Required parameter utm_source is not set");
			}					
			
			if(utm_medium == null) {
				throw new IllegalArgumentException("Required parameter utm_medium is not set");
			}
			
			if(utm_campaign == null || utm_campaign.isEmpty()) {
				throw new IllegalArgumentException("Required parameter utm_campaign is not set");
			}
			
			if(utm_term == null) {
				utm_term = new RandomSet<String>();
			}
			
			if(utm_content == null) {
				utm_content = new RandomSet<String>();
			}
			
			return new UtmReferer(utm_medium, source,utm_campaign, utm_term, utm_content);
		}
	}
	
	private UtmReferer(UtmMedium medium, RandomSet<UtmSource> source, RandomSet<String> campaign, RandomSet<String> term, RandomSet<String> content) {
		this.source = source;
		this.utm_medium = medium;
		this.utm_campaign = campaign;
		this.utm_term = term;
		this.utm_content = content;		
	}
	
	@Override
	public String getReferer() {
		UtmSource src = source.getRandom();
		String campaign = utm_campaign.getRandom();
		String term = (utm_term.isEmpty() ? "" : utm_term.getRandom());
		String content = (utm_content.isEmpty() ? "" : utm_content.getRandom());
		
		StringBuilder sb = new StringBuilder(TextUtils.merge("{0}?utm_source={1}&utm_medium={2}&utm_campaign={3}", src.getUrl(), src.getSource(), utm_medium.getMediumName(), campaign));
		
		if( !Strings.isNullOrEmpty(term) ) {
			sb.append("&utm_term=").append(term);
		}
		
		if( !Strings.isNullOrEmpty(content) ) {
			sb.append("&utm_content=").append(content);
		}
		
		return sb.toString();				
	}

}
