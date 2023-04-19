package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.PluginConstants;


/**
 *
 * @author cwat-moehler
 */
public class StreamingVideo extends AbstractPagePlugin {
	//private static final Logger log = LoggerFactory.make();

	@Override
	public Object doExecute(String location, Object... context)
	{
		if (location.equals(PluginConstants.FRONTEND_MEDIA_HOMEPAGE))
		{
			return getHomepageMedia();
		} else if (location.equals(PluginConstants.FRONTEND_MEDIA_RESOURCE)) {
			return doResource((String) context[0]);
		} else if (location.equals(PluginConstants.FRONTEND_PAGE_RESOURCE)) {
			return doResource((String) context[0]);
		}
		return null;
	}

	public String getHomepageMedia() {
		String styles =
	    	  "<style>"
	    	 + "  #html5_video_stream_container { position: absolute; top: 68px; background-color: #000; width: 560px}"
	       	 + "  #html5_video_stream_container div { color: #fff; font-size: 12px; position: absolute; right: 0; top: 0; width: 130px;}"
	    	 + "</style>";

	    String code
	    	 = "<div id=\"html5_video_stream_container\">"
	    	 + "  <video id=\"html5_video_stream\" controls autoplay=\"true\" height=\"240\" width=\"428\" >"
	         + "     <source src=\"" + getMediaPath("video", "m4v") + "\" />"
	         + "     <source src=\"" + getMediaPath("video", "webm") + "\" type=\"video/webm\" />"
	         + "     <source src=\"" + getMediaPath("video", "theora.ogv") + "\" type=\"video/ogg\" />"
	         + "  </video>"
	         + "  <div><h3>Time lapse sunset</h3> <br />CC-BY, <a href=\"http://www.youtube.com/user/NatureClip\">NatureClip</a></div>"
	    	 + "</div>";

		return styles + code;
	}

    @Override
    public String toString() {
        return "StreamingVideo";
    }

    private String getMediaPath(String filename, String filetype) {
    	return "/plugins/" + getName() + "/media/" + filename + "." + filetype;
    }

}
