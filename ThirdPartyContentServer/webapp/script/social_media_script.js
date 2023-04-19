try {
    window.TPSM || (function(window) {
    	var UNDF = 'undefined';

    	function iframe_create(src) {
    		var iframe = document.createElement('iframe');
    		iframe.setAttribute('src', src);
    		iframe.setAttribute('frameborder', '0');
    		iframe.setAttribute('scrolling', 'no');
    		iframe.setAttribute('marginwidth', '0');
    		iframe.setAttribute('marginheight', '0');
    		iframe.setAttribute('title', 'third party social media');
    		iframe.style.width= 210 + 'px';
    		iframe.style.height = 24 + 'px';
    		return iframe;
    	}

    	var container = document.getElementById('social-media-share');
    	var url = document.getElementById('social-media-share').getAttribute('url');

    	var iframe = iframe_create(url + '/html/social_media_iframe.html');
    	container.appendChild(iframe);

    	var TPSM = {};
    	window.TPSM = TPSM;
    	document.TPSM = TPSM;
    }).call({}, window.inDapIF ? parent.window : window);
} catch (e) {
	alert('error: ' + e);
}