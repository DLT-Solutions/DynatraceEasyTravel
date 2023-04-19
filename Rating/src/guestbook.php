<?php
/*

    $myFile = "test.txt";
	$fh = fopen($myFile, 'a') or die("can't open file");
	fwrite($fh, 'action:'.$action );
*/

/**
 * @author cwat-cchen
 * store,retrieve comments from guestbook and get the number of comments
 */

require("db.php");

/*****************************/

$limit = 5; // No. of records to be shown per page

$db = new DB();
$result = array();
$formatted_comments = array();
$counted_comments = array();

// store guestbook messages
if (array_key_exists('insert', $_POST)) {
	
	$name = $_POST['user_name'];
	$journeyid = $_POST['journey_id'];
	$text = mysql_real_escape_string($_POST['comment']);
	$date = $_POST['date'];
		
	$query= "INSERT INTO guestbook(name, journey_id, comment, comment_date) VALUES('$name',
	         $journeyid, '$text', '$date')";
	
	$db->execute($query);
	
	$result['success'] = "success";
    echo json_encode($result);
    
} 

/**
 * get_rows:  retrieve data form guestbook
 * get_count: return the number of comments in guestbook
 */
if (array_key_exists('action', $_POST)) {
	$action = $_POST['action'];
	$journeyid = $_POST['journey_Id'];
    global $fh;
    	
	 switch($action) {
		case 'get_rows':
			getRows($journeyid);
			break;
		case 'row_count':
			getRowCount($journeyid);	
			break;
		default;
			break;
	} 
		
}

function getRows($journeyid) {
	global $limit;
	
	$start_row = isset($_POST['start'])?$_POST['start']: 0;
	$start_row = $limit * (int)$start_row;
	global $fh;
	
	$comments = loadComments($start_row, $journeyid);	
	$formatted_comments['comment'] = "<div id='formatted_comments'>".formatComments($comments)."</div>";
		
	echo json_encode($formatted_comments);	
}

function loadComments($start_row, $journeyid) {
	global $db, $limit;
	
	$query = "SELECT * FROM guestbook where journey_id=$journeyid ORDER BY no DESC LIMIT $start_row, $limit";
	$comments = $db->queryData($query);
	
	return $comments;
}

function formatComments($comments) {
	$formatted ='';
	
	if(!empty($comments))
	{
		$formatted.= "<span class =\"heading\">User Reviews</span>";		
	}
	
	foreach ($comments as $data) {
		$formatted.= '<p><strong>'.$data['name'].'</strong> - '.$data['comment_date'].'</p>'
				.'<p>'.$data['comment'].'</p><hr />';		
	}
	
	return $formatted;
}

function getRowCount($journeyid) {
	global $db, $limit;

	$query = "SELECT COUNT(*) AS COUNT FROM guestbook where journey_id=$journeyid";
	$count = $db->countOfData($query);
	
	$counted_comments['count'] = $count;
	$counted_comments['limit'] = $limit;
	
	echo json_encode($counted_comments);
	
}
?>