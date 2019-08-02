<?php


function startsWith($haystack, $needle)
{
     $length = strlen($needle);
     return (substr($haystack, 0, $length) === $needle);
}

function endsWith($haystack, $needle)
{
    $length = strlen($needle);

    return $length === 0 || 
    (substr($haystack, -$length) === $needle);
}


error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_errors', 1);
ini_set("log_errors", 1);
ini_set("error_log", __DIR__ . "/forward-http-to-google-analytics.log");
$BLOCKED_IP = '92.92.61.14';

if($_SERVER['REMOTE_ADDR'] != $BLOCKED_IP){
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, "http://www.google-analytics.com/collect");
	curl_setopt($ch, CURLOPT_POST, 1);
	curl_setopt($ch, CURLOPT_TIMEOUT, 100);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($_POST));
	curl_exec($ch);
	$curl_info = curl_getinfo($ch);
	curl_close($ch);
}

$myfile = fopen( __DIR__ . "/last-http-request.log", "w") or die("Unable to open file!");
fwrite($myfile, "_SERVER: ");
fwrite($myfile, print_r($_SERVER, true));
fwrite($myfile, PHP_EOL);
fwrite($myfile, "_POST: ");
fwrite($myfile, print_r($_POST, true));
fwrite($myfile, PHP_EOL);
fwrite($myfile, "curl_info: ");
fwrite($myfile, print_r($curl_info, true));
fclose($myfile);

if($_SERVER['REMOTE_ADDR']  != $BLOCKED_IP){	
	if(startsWith($_POST["ec"], "PhoyoId")){	
		$TRACKING_LOG_FILE = "/phoyoID-trackings.log";
	}else{
		if(strpos($_POST["ec"], 'Pro') !== false){
			$TRACKING_LOG_FILE = "/trackings-pro.log";
		}else{
			$TRACKING_LOG_FILE = "/trackings.log";
		}
	}
}else{
	if(startsWith($_POST["ec"], "PhoyoId")){	
		$TRACKING_LOG_FILE = "/phoyoID-trackings-blocked.log";
	}else{		
		if(strpos($_POST["ec"], 'Pro') !== false){
			$TRACKING_LOG_FILE = "/trackings-pro-blocked.log";
		}else{
			$TRACKING_LOG_FILE = "/trackings-blocked.log";
		}
	}
}
$myfile = fopen( __DIR__ . $TRACKING_LOG_FILE, "a") or die("Unable to open file: " . $TRACKING_LOG_FILE);
$EVENT_TIME_SECONDS = microtime(true) - (floatval($_POST["qt"])/1000.0);
date_default_timezone_set('Europe/Paris');
$EVENT_TIME = date( "Y-m-d H:i:s",  $EVENT_TIME_SECONDS);
#$USER_ID = $_SERVER['REMOTE_ADDR'];
$USER_ID = $_POST["cid"];
$CATEGORY = $_POST["ec"];
$ACTION = $_POST["ea"];
$LABEL = $_POST["el"];
fwrite($myfile, $EVENT_TIME . "	" . $USER_ID . "	" . $CATEGORY . "	" . $ACTION . "	" . $LABEL);
fwrite($myfile, PHP_EOL);
fclose($myfile);

?>
