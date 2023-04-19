<?php
error_reporting(E_ALL ^ E_DEPRECATED);

/**
 * @author cwat-cchen
 * access MySQL with convenient methods in an object-oriented way
 */
$database = "easytravel";

Class DB {
  private $link;
  
  /**
   * connect to a MySQL database
   */
  function __construct() {
	global $database;
    $this->link = mysqli_connect('localhost', 'root', 'labpass') or die('Failed to connect to mysqli database server!');
	mysqli_select_db($this->link, $database) or die('Guestbook cannot be accessed or does not exist');
  }

  function execute($query) {
  	return $result = mysqli_query($this->link, $query);
  }
  
  function queryData($query) {
  	$result = $this->execute($query);
  	$data = array();
  	
  	while($row = mysqli_fetch_assoc($result)) {
  		$data[] = $row;
  	}
  	
  	return $data;
  }
  
  function querySingleRow($query) {
  	$result = $this->execute($query);
  	$data = array();
  	 
  	if ($row = mysqli_fetch_assoc($result)) {
  		return $row;
  	}
  	 
  	return null;
  }
  
  function countOfData($query) {
  	$result = $this->execute($query);
  	$count = mysqli_fetch_row($result);
  	
  	return $count[0];
  }
}

?>