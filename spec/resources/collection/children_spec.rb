require 'spec_helper'

describe 'Getting the children of a collection' do
  context 'existing children' do
    let :collection_with_children do
      collection = FactoryGirl.create(:collection,
                                      get_metadata_and_previews: true)
      collection.media_entries << FactoryGirl.create(:media_entry)
      collection.collections << FactoryGirl.create(:collection)
      collection.filter_sets << FactoryGirl.create(:filter_set)
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

    it 'responds with 200 for filter-sets' do
      relation = json_roa_keyword_resource.get.relation('filter-sets').get
      expect(relation.response.status).to be == 200
      expect(relation.data['filter-sets'].length).to be == 1
    end
  end

  context 'no children' do
    let :collection_without_children do
      FactoryGirl.create(:collection,
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

    it 'responds with 200 for filter-sets' do
      relation = json_roa_keyword_resource.get.relation('filter-sets').get
      expect(relation.response.status).to be == 200
      expect(relation.data['filter-sets'].length).to be == 0
    end
  end
end
