/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.model.Mindmap = new Class({
        initialize : function() {
            this._branches = [];
            this._description = null;
            this._version = null;
            this._relationships = [];
        },

        getCentralTopic : function() {
            return this._branches[0];
        },

        getDescription : function() {
            return this._description;
        },

        getId : function() {
            return this._iconType;
        },


        setId : function(id) {
            this._iconType = id;
        },

        getVersion : function() {
            return this._version;
        },


        setVersion : function(version) {
            this._version = version;
        },

        addBranch : function(nodeModel) {
            $assert(nodeModel && nodeModel.isNodeModel(), 'Add node must be invoked with model objects');
            if (this._branches.length == 0) {
                $assert(nodeModel.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE, "First element must be the central topic");
                nodeModel.setPosition(0, 0);
            } else {
                $assert(nodeModel.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE, "Mindmaps only have one cental topic");
            }

            this._branches.push(nodeModel);
        },

        getBranches : function() {
            return this._branches;
        },

        getRelationships : function() {
            return this._relationships;
        },

        connect : function(parent, child) {
            // Child already has a parent ?
            var branches = this.getBranches();
            $assert(!child.getParent(), 'Child model seems to be already connected');

            //  Connect node...
            parent._appendChild(child);

            // Remove from the branch ...
            branches.erase(child);
        },

        disconnect : function(child) {
            var parent = child.getParent();
            $assert(child, 'Child can not be null.');
            $assert(parent, 'Child model seems to be already connected');

            parent._removeChild(child);

            var branches = this.getBranches();
            branches.push(child);

        },

        hasAlreadyAdded : function(node) {
            var result = false;

            // Check in not connected nodes.
            var branches = this._branches;
            for (var i = 0; i < branches.length; i++) {
                result = branches[i]._isChildNode(node);
                if (result) {
                    break;
                }
            }
        },

        createNode : function(type, id) {
            $assert(type, "node type can not be null");
            return this._createNode(type, id);
        },

        _createNode : function(type, id) {
            $assert(type, 'Node type must be specified.');
            var result = new mindplot.model.NodeModel(type, this, id);
            return result;
        },

        createRelationship : function(fromNode, toNode) {
            $assert(fromNode, 'from node cannot be null');
            $assert(toNode, 'to node cannot be null');

            return new mindplot.model.RelationshipModel(fromNode, toNode);
        },

        addRelationship : function(relationship) {
            this._relationships.push(relationship);
        },

        removeRelationship : function(relationship) {
            this._relationships.erase(relationship);
        },

        inspect : function() {
            var result = '';
            result = '{ ';

            var branches = this.getBranches();
            for (var i = 0; i < branches.length; i++) {
                var node = branches[i];
                if (i != 0) {
                    result = result + ', ';
                }

                result = result + this._toString(node);
            }

            result = result + ' } ';

            return result;
        },

        _toString : function(node) {
            var result = node.inspect();
            var children = node.getChildren();

            for (var i = 0; i < children.length; i++) {
                var child = children[i];

                if (i == 0) {
                    result = result + '-> {';
                } else {
                    result = result + ', ';
                }

                result = result + this._toString(child);

                if (i == children.length - 1) {
                    result = result + '}';
                }
            }

            return result;
        }
    }
);