<!DOCTYPE HTML>
<%@page import="org.json.JSONArray, org.json.JSONObject, org.apache.commons.lang.StringEscapeUtils,  
        co.mybridge.MBPeople, co.mybridge.MBCollections, co.mybridge.DBUtils" %>
<%
    MBPeople mp = new MBPeople();
    JSONArray javaPeople = DBUtils.retrieveObjects(null, "mb_person", mp, "no");
    MBCollections mc = new MBCollections();
    JSONArray javaCollections = DBUtils.retrieveObjects(null, "mb_collection", mc, "no");
%>
<html>
<head>
   <style>
    /* all */
    ::-webkit-input-placeholder { color:#f00; }
    ::-moz-placeholder { color:#f00; } /* firefox 19+ */
    :-ms-input-placeholder { color:#f00; } /* ie */
    input:-moz-placeholder { color:#f00; }
  </style>
  <script language="JavaScript">
  jsCollections=eval('<%=StringEscapeUtils.escapeJavaScript(javaCollections.toString())%>');
  jsPeople=eval('<%=StringEscapeUtils.escapeJavaScript(javaPeople.toString())%>');
  </script>
  <script language="JavaScript">
  function autoPSSubmit() {
      document.getElementById("submit").disabled = true;
      var pid = document.getElementById("personId").value;
      if (pid.length > 5) {
          document.personform.action="/api/people/" + pid;
      }
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("submit").disabled = false;
      document.getElementById("personform").submit();
  }
  function autoCollSubmit() {
      document.getElementById("collsubmit").disabled = true;
      var pid = document.getElementById("collPersonId").value;
      if (pid.length > 5) {
          document.collectionform.action="/api/people/" + pid + "/collections";
      }
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("collsubmit").disabled = false;
      document.getElementById("collectionform").submit();
  }
  function autoKnowlSubmit() {
      document.getElementById("knowlsubmit").disabled = true;
      var pid = document.getElementById("knowlPersonId").value;
      var cid = document.getElementById("collectionId").value;
      if (pid.length > 5 && cid.length > 5) {
          document.knowledgeform.action="/api/people/" + pid + "/collections/" + cid + "/knowledge/";
      }
      // enable the button right before the submit, otherwise, the submit does not work.
      document.getElementById("knowlsubmit").disabled = false;
      document.getElementById("knowledgeform").submit();
  }
  function autoFollowSubmit() {
      document.getElementById("followsubmit").disabled = true;
      var pid = document.getElementById("followerId").value;
      if (pid.length > 5 ) {
          document.followform.action="/api/people/" + pid + "/following/";
      }
      document.getElementById("followsubmit").disabled = true;
      document.getElementById("followform").submit();
  }
  
  function toggleContentType(formElem, collDropDown) {
      var coll = collDropDown.value;
      if (coll != null && coll.length > 1) {
          if (collDropDown.name == "collectionId") {
          	 formElem.contentType.value="collections";
          	 populateKnowledge(formElem, collDropDown);
          	 document.getElementById("followperson").style.display="none";
          	 document.getElementById("followcollection").style.display="block";
          	 document.getElementById("favoriteknowledge").style.display="none";
          } else if (collDropDown.name == "knowledgeId") {
             formElem.contentType.value="knowledge";
             document.getElementById("followperson").style.display="none";
          	 document.getElementById("followcollection").style.display="none";
          	 document.getElementById("favoriteknowledge").style.display="block";
          }
      } else {
          if (collDropDown.name == "collectionId") {
          	formElem.contentType.value="people";
          	document.getElementById("followperson").style.display="block";
          	 document.getElementById("followcollection").style.display="none";
          	 document.getElementById("favoriteknowledge").style.display="none";
          } else {
             formElem.contentType.value="collections";
             document.getElementById("followperson").style.display="none";
          	 document.getElementById("followcollection").style.display="block";
          	 document.getElementById("favoriteknowledge").style.display="none";
          	 populateKnowledge(formElem, collDropDown);
          }
      }
  }
  /**
   *  this will populate collection options with collectionId in this form
   */
  function populateCollections(formElem, personDropDown) {
      var pid = personDropDown.value;
      var selectElem = formElem.collectionId;
      // remove everything except the first one
      selectElem.options.length = 1;
      
      for (var x=0; x<jsCollections.length; x++) {
           
          var coll = jsCollections[x];
          var collpid = coll.personId;
          if (collpid == pid) {
              var collOpt = document.createElement("option");
              collOpt.text = coll.collectionTitle;
              collOpt.value = coll._id;
              selectElem.add(collOpt);
          }
      }
  }
   function populateKnowledge(formElem, collDropDown) {
      var cid = collDropDown.value;
      var selectElem = formElem.knowledgeId;
      // remove everything except the first one
      selectElem.options.length = 1;
      
      for (var x=0; x<jsCollections.length; x++) {
           
          var coll = jsCollections[x];
          var collcid = coll._id;
          if (collcid == cid) {
              var knowledges = coll.knowledge;
              for (var k = 0; k< knowledges.length; k++) {
                  var collOpt = document.createElement("option");
                  collOpt.text = knowledges[k].customDescription;
                  collOpt.value = knowledges[k].knowledgeId;
                  selectElem.add(collOpt);
              }
          }
      }
  }
  function displayExistingColls(personDropDown) {
      var pid = personDropDown.value;
      var collOpt = "";
      for (var x=0; x<jsCollections.length; x++) {
           
          var coll = jsCollections[x];
              var collpid = coll.personId;
              if (collpid == pid) {
                  var collTitle = coll.collectionTitle;
                  if (collOpt == "") {
                      collOpt = collTitle;
                  } else {
                      collOpt = collOpt + '&nbsp;;&nbsp;&nbsp;' + collTitle;
                  }
              }
      }
      document.getElementById("existColls").innerHTML = collOpt;
  }
  function populatePerson(personDropDown) {
      var pid = personDropDown.value;
      for (var x=0; x<jsPeople.length; x++) {    
          var coll = jsPeople[x];
          var jspid = coll._id;
          if (jspid == pid) {
              var email = coll.email;
              var fullName = coll.fullName;
              var position = coll.position;
              var company = coll.company;
              var thumbImage = coll.thumbImage;
              var education = coll.education;
              var professions = coll.professions;
              
              if (email != null && email.length > 1) {
              	  personform.email.value = email;
              } else {
                  personform.email.value = '';
              }
              personform.fullName.value = fullName;
              if (position != null && position.length > 0) {
                  personform.position.value = position;
              } else {
                  personform.position.value = '';
              }
              if (company != null && company.length > 0) {
                  personform.company.value = company;
              } else {
                  personform.company.value = '';
              }
              personform.thumbImage.value = thumbImage;
              if (education != null && education.length > 1) {
                  personform.education.value = education;
              } else {
                  personform.education.value = '';
              }
                 
              var profform = personform.profession;
              for (j=0; j< profform.length; j++) {
                  profform[j].checked = false;
              }
              if (professions.length > 0) {
                  for (i=0; i<professions.length; i++) {
                 		var prof = professions[i];
                   		for (j=0; j< profform.length; j++) {
                   		    if (profform[j].value == prof) {
                   		        profform[j].checked = true;
                   		    }
                   		}
                  }
              }
              break;
          }
      }
  }
  </script>
</head>
<body>
<h1>Testing data entry, utilizing add/modify APIs</h1>
<div id="sec1">
<h2>Add or modify a user</h2>
<form name="personform" id="personform" action="/api/people" method="post">
<table width="90%" border="0" align="center">
<tr><td colspan="4"><h2>
<select name="personId" id="personId" onchange="populatePerson(this);">
<option value="">Create a new user</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">Update   <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</h2></td></tr>

<tr><th> Email</th><th>Password</th><th>Name</th><th>Industry</th><th>Profession</th></tr>
<tr><td align="center">
<input type="text" name="email" size="20" placeholder="email"> </input>
</td><td align="center">
<input type="password" name="password" size="14" placeholder="password"> </input>
</td>
<td align="center">
<input type="text" name="fullName" size="14" placeholder="Your Name"> </input>
</td><td align="center">
<select id="industry" name="industry">
    <option value="Internet/Computer Software">Internet/Computer Software</option>
</select>
</td><td align="left">
<input type="checkbox" name="profession" value="Startup">Start Up</input><br>
<input type="checkbox" name="profession" value="CEO">CEO</input><br>
<input type="checkbox" name="profession" value="Sales">Sales</input><br>
<input type="checkbox" name="profession" value="Marketing">Marketing</input><br>
<input type="checkbox" name="profession" value="Developer">Developer</input>
</td></tr>
<tr><td align="left" colspan="4">&nbsp;&nbsp;&nbsp;<b>Profile image URL:</b> 
<input type="text" name="thumbImage" size="45" placeholder="Enter Image URL here"></input>
</td></tr>
<tr><td colspan="2" >&nbsp;&nbsp;&nbsp;<b>Position:</b> <input type="text" size="20" name="position" placeholder="optional"></input></td>
<td colspan="2" ><b>Company:</b> <input type="text" size="20" name="company" placeholder="optional"></input></td>
<td><b>Education: </b><input type="text" size="10" name="education" placeholder="optional"></input></td></tr>
<tr><td colspan="5" align="center"><input type="submit" value="Submit" id="submit" name="submit" 
    onClick="javascript:autoPSSubmit();"></td></tr>
</table>
</form>
</div>
<br>
<hr>
<div id="sec2">
<h2>Add a collection</h2>

<form name="collectionform" id="collectionform" action="/api/people/" method="post">
<table width="80%" border="0" align="center">
<tr><td >
<select name="collPersonId" id="collPersonId" onchange="displayExistingColls(this);">
<option value="" disabled selected style="display:none;">Select a person</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select>
</td></tr>
<tr><td><b>Existing collections: </b><span id="existColls">  </span><td><tr>
<tr><td>New collection title: <input type="text" name="collectionTitle" size="40" value=""></td></tr>

<tr><td align="center"><input type="submit" value="Submit" id="collsubmit" name="collsubmit" 
      onClick="javascript:autoCollSubmit();"></td></tr>
</table>
</form>

</div>
<hr>
<div id="sec3">
<h2>Add a knowledge to a collection</h2>
<form name="knowledgeform" id="knowledgeform" action="/api/people/" method="post">
<table width="80%" border="0" align="center">
<tr><td >
<select name="knowlPersonId" id="knowlPersonId" onchange="populateCollections(document.knowledgeform, this);">
<option value="" disabled selected style="display:none;">Select a person</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>">knowledge for  <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select></td>
<td >
<select name="collectionId" id="collectionId">

</select></td>
</tr>

<tr><td>Knowledge Title: </td><td><input type="text" name="title" size=60" value=""></td></tr>
<tr><td>My Own description: </td><td> <input type="text" name="customDescription" size="60" value=""></td></tr>
<tr><td>From Source: </td><td> <input type="text" name="contentSource" size="20"  value="YouTube"></td></tr>
<tr><td>Publisher: </td><td> <input type="text" name="publisher" size="40"  value=""></td></tr>
<tr><td>Type: </td><td> <input type="text" name="contentType" size="20"  value="Video"></td></tr>
<tr><td>External URL:  </td><td><input type="text" name="externalURL" size="70" value=""></td></tr>
<tr><td>HTML Body:  </td><td><input type="text" name="htmlBody"  size="70" value=""></td></tr>
<tr><td>Thumb URL:  </td><td><input type="text" name="thumbImage"  size="60" value=""></td></tr>
<tr><td>Content Width:  </td><td><input type="text" name="width"  size="8" value=""></td></tr>
<tr><td>Content Height:  </td><td><input type="text" name="height"  size="8" value=""></td></tr>
<tr><td align="center" colspan=2><input type="submit" value="Submit" id="knowlsubmit" name="knowlsubmit" 
onClick="javascript:autoKnowlSubmit();"></td></tr>
</table>
</form>

</div>

<hr>
<div id="sec4">
<h2>Follow a person or a collection</h2>
<form name="followform" id="followform"  action="/api/people/" method="post">
<table width="80%" border="0" align="center">
<tr><td colspan=3> Current user :
<select name="followerId" id="followerId" >
<option value="" disabled selected style="display:none;">Select a person</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
        if (jobj.has("email") && jobj.has("password") && jobj.getString("email").length() > 1) {
%>
            <option value="<%= jobj.getString("_id") %>"> <%= jobj.getString("fullName") %></option>
<%
        }
    }      
%>

</select></td></tr>
<tr><td><span id="followperson" name="followperson" style="display:block"><b>Follow a person</b></span></td>
<td><span id="followcollection" name="followcollection" style="display:none"><b>Follow a collection</b></span></td>
<td><span id="favoriteknowledge" name="favoriteknowledge" style="display:none"><b>Favorite knowledge</b></span></td></tr>
<tr><td >
<select name="followedId" id="followedId" onchange="populateCollections(document.followform, this);" >
<option value="" disabled selected style="display:none;">Select a person to follow</option>
<%
    for (int i = 0; i< javaPeople.length(); i++) {
        JSONObject jobj = javaPeople.getJSONObject(i);
%>
        <option value="<%= jobj.getString("_id") %>"> <%= jobj.getString("fullName") %></option>
<%
    }      
%>

</select></td>
<td > 
<select name="collectionId" id="collectionId" onchange="toggleContentType(document.followform, this);">
<option value=""  selected >Follow all collections of this person</option>

</select></td>
<td > 
<select name="knowledgeId" id="knowledgeId" onchange="toggleContentType(document.followform, this);">
<option value=""  selected >Follow all contents of this collection</option>

</select></td>
</tr>
<tr><td colspan=2 >&nbsp;&nbsp;&nbsp;
<input type=hidden name="contentType" id="contentType" value="people">
<input type=submit value="Submit" name="followsubmit" id="followsubmit" onclick="javascript:autoFollowSubmit();">
</td></tr>
</table>
</form>
</form>
</div>
<br>
<hr>
<div>
<h2>Sample page to demonstrate login API</h2>
<form method="POST" action="/api/login">
<table width="85%" border=0 align="center">
<tr><td>
Email: </td><td><input type=text name="email"></td></tr>
<tr><td>Password: </td><td><input type=text name="password"></td></tr>
<tr><td colspan=2>&nbsp;&nbsp;<input type=submit name="Submit" value="Submit"></td></tr>
</table>
</form>
</div>
<hr>
<div>
<h2>Extracting contents from URL</h2>
<form method="GET" action="/api/urlextractor">
<table width="85%" border=0 align="center">
<tr><td>
URL: <input type=text name="url" size="80"></td>
&nbsp;&nbsp;<input type=submit name="Submit" value="Submit"></td></tr>
</table>
</form>
</body></html>
