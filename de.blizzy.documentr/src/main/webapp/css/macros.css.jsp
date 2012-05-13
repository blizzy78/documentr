/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* {{children/}} */

.children-box {
	background-color: #e5f3fa;
	border: 1px solid rgba(0, 0, 0, 0.05);
	border-radius: 4px 4px 4px 4px;
	box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05) inset;
	padding: 10px;
	float: right;
	margin-left: 20px;
	margin-bottom: 20px;
}

.children-box ul.children {
	margin: 0 0 0 25px;
}

.children-box li.active {
	font-weight: bold;
}

.children-box li.active ul {
	font-weight: normal;
}