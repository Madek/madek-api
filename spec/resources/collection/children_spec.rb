require 'spec_helper'

describe 'Getting the children of a collection' do
  context 'existing children' do
    let :collection_with_children do
      collection = FactoryBot.create(:collection,
                                      get_metadata_and_previews: true)
      collection.media_entries << (@me = FactoryBot.create(:media_entry, get_metadata_and_previews: true))
      collection.collections << FactoryBot.create(:collection)
      collection
    end

    let :json_roa_keyword_resource do
      JSON_ROA::Client.connect \
        "#{api_base_url}/collections/#{collection_with_children.id}"
    end

    it 'responds with 200 for media-entries' do
      relation = json_roa_keyword_resource.get.relation('collections').get
      expect(relation.response.status).to be == 200
      expect(relation.data['collections'].length).to be == 1
    end

    it 'responds with 200 for collections' do
      relation = json_roa_keyword_resource.get.relation('media-entries').get
      expect(relation.response.status).to be == 200
      expect(relation.data['media-entries'].length).to be == 1
    end

    context 'collection-media-entry-arcs' do

      it 'is accessible' do
        relation = json_roa_keyword_resource.get.relation('collection-media-entry-arcs').get
        expect(relation.response.status).to be == 200
        expect(relation.data['collection-media-entry-arcs'].length).to be == 1
      end

      it 'links for a round trip "collection → arc → entry → arc → collection" ' \
        ' can be traversed and retrieve the original collection' do
        media_entry_resource = json_roa_keyword_resource.get.relation('collection-media-entry-arcs') \
          .get.collection.first.get.relation('media-entry').get
        collection_resource = media_entry_resource.relation('collection-media-entry-arcs') \
          .get.collection.first.get.relation('collection').get
        expect(@me[:id]).to be== media_entry_resource.data[:id]
        expect(collection_with_children[:id]).to be== collection_resource.data[:id]
      end

    end

  end

  context 'no children' do
    let :collection_without_children do
      FactoryBot.create(:collection,
                         get_metadata_and_previews: true)
    end

    let :json_roa_keyword_resource do
      JSON_ROA::Client.connect \
        "#{api_base_url}/collections/#{collection_without_children.id}"
    end

    it 'responds with 200 for media-entries' do
      relation = json_roa_keyword_resource.get.relation('collections').get
      expect(relation.response.status).to be == 200
      expect(relation.data['collections'].length).to be == 0
    end

    it 'responds with 200 for collections' do
      relation = json_roa_keyword_resource.get.relation('media-entries').get
      expect(relation.response.status).to be == 200
      expect(relation.data['media-entries'].length).to be == 0
    end

  end
end
