<?php
/**
 *
 * @author cwat-cchen
 * store and get ratings
 * 
 */

require("db.php");

/*****************************/

/* read request parameters from a Java application and
 * and convert it into a PHP variable
*/
if (!isset($_POST['journey_id'])) {
	$input = file_get_contents("php://input");
	$data = json_decode($input,true);
	$_POST = $data;
	$rating = new ratings($_POST['journey_id']);	
}

/*
 * check if the php server is running
 */
if (isset($_GET['on'])) {
	
	$on = $_GET['on'];
	$data = json_decode($on);
	$check = $data -> {"check"};

	if($check == 1) {
		$array = array("check" => "ok");
		echo json_encode($array);
	}	
}

$db = new DB();

if(isset($_POST)) {
	$rating = new ratings($_POST['journey_id']);
}
/**
 * @param fetch: return the individual rating, ex. staff, services...etc.
 * @param info:  return the average rating of each journey
 * @param diff:  return the HTML code of rating details 
 * @param Neither None of those params:  store ratings
 */
if (array_key_exists('fetch', $_POST)) {
	echo json_encode($rating->get_ratings());
} else if (isset($_POST['info'])) {
    echo json_encode($rating->totalAvg()); 
} else if (isset($_POST['diff'])) {
    echo json_encode(getRatingDetailsHTML($rating));
} else {
	echo json_encode($rating->vote());
}

function getRatingDetailsHTML($rating) {

	
	$totalAvg = $rating->totalAVG();
	$journeyid = $rating->getJourneyID();
	
	$formatted_ratings['ratings'] = "<div id='formatted_ratings'>".formatRatings($totalAvg,$journeyid)."</div>";
	
	return $formatted_ratings;
}

function formatRatings($partialRatings, $journeyid) {
	$formatted ='';
	
	$formatted.="<table width=\"500\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\"  class=\"spec\">
	<tr><td colspan=\"2\" align=\"center\"  bgcolor=\"#4ABFD2\" style=\"align: center; height:20pt\"><span style=\"color:white\"><strong>Rating Details</strong></span></td></tr>
	<tbody><tr bgcolor=\"#E8EDFF\">
	<td style=\"width:20%; border-right: 1px solid #C1DAD7; align:center\"><h3>Total Journey Score:</h3></td>
	<td style=\"padding-left:10px\"><h3> Score breakdown:</h3></td>
	</tr>";
	
	$formatted.="<tr bgcolor=\"#E8EDFF\"><td style=\"width:20%; border-right: 1px solid #C1DAD7; align:center\">
	<div class=\"total\" >
	<strong ><div class=\"avg_rating\">{$partialRatings['total_avg']}</div></strong></div><Br/>
	<strong ><div class=\"avg_votes\">Based On {$partialRatings['reviews']} reviews</div></strong>
	<p id=\"rev_disclaimer\">Guest reviews are written by our customers <strong>after their stay</strong>. </p>
	</td>";
	
	$formatted.=formatIndividualRating($journeyid);
	$formatted.="</tr><tr><td colspan=\"2\" style=\"border-bottom: 1px solid #CCC !important;\"></td></tr>	
	    </tbody></table>";
	
	return $formatted ;
}

function formatIndividualRating($journeyid) {
	global $database, $server, $user, $password, $db;
	$formateed='';
	
    $category = array(
        0 => "staff",
        1 => "services",
        2 => "cleanliness",
        3 => "comfort",
        4 => "value",
        5 => "location" );
	
	$formateed.="<td style=\"width:25%; border: 0px solid #C1DAD7; align:center\">";
	
	$db = new DB($database, $server, $user, $password);
	$queryAll = "SELECT * FROM rating WHERE journey_id like '$journeyid%'";
	$query = "SELECT COUNT(*) AS COUNT FROM rating WHERE journey_id like '$journeyid%'";
	$result= $db->queryData($queryAll);
	$count = $db->countOfData($query);
	
	$formateed.="<ul>";
	for( $i=0; $i < 6; $i++ ) {	
		$jid = $journeyid.'_'.$category[$i];
		
		if($pos = checkIndividualRating($jid,$result, $count)) {
		    $pos--;
			$formateed.="<li> <div class=\"{$jid}\" style=\"margin: 0; height: 20px;\"><strong>{$category[$i]}:
			{$result[$pos]['dec_avg']}</strong></div></li>";
		} else {
			$formateed.="<li> <div class=\"{$jid}\" style=\"margin: 0; height: 20px;\"><strong>{$category[$i]}:
			0 </strong></div></li>";
		}
	}
	
	$formateed.="</ul></td>";
	
	return $formateed;
}

function checkIndividualRating($category, $result, $count ) {
	$check= 0;
	
	for ($i=1; $i <= $count; $i++) {
		if(in_array("{$category}",$result[$i-1])){
			$check = $i;
			break;
		}
	}
	
	return $check;
}

class ratings {   
    private $journey_id;
    private $data ; 
       
	/**
	 * Connect to a MySQL database and query the database
	 * @param $jid The journey id
	 */
	function __construct($jid) {
		global $database, $server, $user, $password, $db;
		
		$db = new DB($database, $server, $user, $password);
		$query = "SELECT * FROM rating WHERE journey_id like '$jid%'";
		$this->journey_id = $jid;
			
	    $this->data = $db->querySingleRow($query);			
	    
	}
	
	/**
	 * If data is set, return its rating, otherwise return 0
	 */
	public function get_ratings() {
		
	    if (isset($this->data)) {
	    	$this->data['journey_id'] = $this->journey_id;
	        return $this->data;
	    }
	    else {
	        $this->data['journey_id'] = $this->journey_id;
	        $this->data['number_votes'] = 0;
	        $this->data['total_points'] = 0;
	        $this->data['dec_avg'] = 0;
	        $this->data['whole_avg'] = 0;
			$this->data['voting']= 0;
	        return $this->data;
	    } 
	}
	
	/**
	 * Get the value of the vote, create and update the record
	 */
	public function vote() 
	{
		global $db;
		$query;
		
		# Get the value of the vote
	    preg_match('/star_([1-5]{1})/', $_POST['clicked_on'], $match);
	    $vote = $match[1];
	    
	    $ID = $this->journey_id;
	    
	    # Update the record if it exists
	    if(isset($this->data)) {	    	
	        $this->data['number_votes'] += 1;
	        $this->data['total_points'] += $vote;
			$this->data['voting'] = $vote;
			$this->data['dec_avg'] = round( $this->data['total_points'] / $this->data['number_votes'], 1 );
			$this->data['whole_avg'] = round( $this->data['dec_avg'] );
			
			$query = "UPDATE rating SET number_votes= {$this->data['number_votes']}, total_points=
			{$this->data['total_points']}, voting= {$this->data['voting']}, dec_avg= {$this->data['dec_avg']},
			whole_avg=  {$this->data['whole_avg']} WHERE journey_id= '$this->journey_id'";
			
			$db->execute($query);
			 
	    }
	    # Create a new one if it doesn't
	    else {
	        $this->data['number_votes'] = 1;
	        $this->data['total_points'] = $vote;
			$this->data['voting'] = $vote;
			$this->data['dec_avg'] = round( $this->data['total_points'] / $this->data['number_votes'], 1 );
			$this->data['whole_avg'] = round( $this->data['dec_avg'] );
			
			$query = "INSERT INTO rating (journey_id, number_votes, total_points, dec_avg, whole_avg,
			          voting) VALUES ('$this->journey_id', {$this->data['number_votes']},
			          {$this->data['total_points']}, {$this->data['dec_avg']},
			          {$this->data['whole_avg']}, {$this->data['voting']})";

		   $db->execute($query);
	    }
	                 
	    return $this->get_ratings();
	}
	
	/**
	 * Add up all of the $result['dec_avg'] and get the average
	 */
	public function totalAvg() 
	{
	     global $db;
	     
		 $ID = $this->journey_id;	 
		 $data = array();
		 $result = array();
		
		 $total = 0;
	     $reviews = 0;
	     $query = "SELECT COUNT(*) AS COUNT FROM rating WHERE journey_id like '$ID\_%'";
	     $count = $db->countOfData($query); 
	     	     
	     if($count == 0) {
	     	$result['journey_id'] = $ID;
	     	$result['reviews']= 0;
	     	$result['total_avg'] = 0;
	     	
	     } else{
	     	     	 
	     	 $queryAll = "SELECT * FROM rating WHERE journey_id like '$ID\_%'";
	     	 $data = $db->queryData($queryAll);
	     	
	     	 # get the average rating of other subitems
	     	 for ($i=0; $i<$count; $i++ ) {
	     	     $total+= $data[$i]['dec_avg'];
	     	     $temp = $data[$i]['number_votes'];
	        
	     	     if ( $temp > $reviews )
			         $reviews = $temp;     	     
	     	 } 
	     	
	     	 $result['journey_id'] = $ID;
	     	 $result['reviews']= $reviews;
	     	 $result['total_avg'] = round( $total/6, 1); 
	     	
	     }
		 
	     return $result;	
	}
	
	public function getJourneyID() {
		return $this->journey_id;
	}
}
?>