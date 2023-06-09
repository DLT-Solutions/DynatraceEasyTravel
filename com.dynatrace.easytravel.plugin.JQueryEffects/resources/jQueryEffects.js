$(".iceCmdBtn").click(function() {
	// $(".iceDatTblCol2").animate({ backgroundColor: "#68BFEF" }, 500);
	applyGlobalStyle();
});

// we iterate through all elements and apply styles
function applyGlobalStyle() {
	var elements = $("*");
	for(i=0;i<elements.length;i++) {
		if($(elements[i]).hasClass("iceDatTblCol2")) {
			$(elements[i]).animate({ backgroundColor: "#68BFEF" }, 500);
		}
		
		if(elements[i].tagName == 'A') {
			$(elements[i]).hover(function() {
				$(this).animate({ backgroundColor: "#68BFEF" }, 500);
			}, function() {
				$(this).animate({ backgroundColor: "#FFFFFF" }, 500);
			});
		}
	}
};

applyGlobalStyle();