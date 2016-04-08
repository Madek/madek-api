require 'spec_helper'

describe 'index' do
  include_context :json_roa_client_for_authenticated_user do
    let :vocabularies_resource do
      json_roa_client.get.relation('vocabularies').get
    end

    it 'should return 200 with only viewable by public vocabularies' do
      FactoryGirl.create(:vocabulary, enabled_for_public_view: false)
      expect(vocabularies_resource.response.status).to be == 200
      expect(vocabularies_resource.data['vocabularies'].count).to be == 0
    end
  end
end
