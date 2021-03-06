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

    context 'when user is authenticated' do
      context 'when view permission is true' do
        it 'returns vocabulary in collection through the user permissions' do
          vocabulary = FactoryGirl.create(:vocabulary,
                                          enabled_for_public_view: false)
          Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                                     view: true,
                                                                     vocabulary: vocabulary)

          data = client.get.relation('vocabularies').get.data['vocabularies'].first

          expect(data).to have_key 'id'
          expect(data['id']).to eq vocabulary.id
        end

        it 'returns vocabulary in collection through the group permissions' do
          vocabulary = FactoryGirl.create(:vocabulary,
                                          enabled_for_public_view: false)
          group = FactoryGirl.create :group
          group.users << user
          Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                                      view: true,
                                                                      vocabulary: vocabulary)

          data = client.get.relation('vocabularies').get.data['vocabularies'].first

          expect(data).to have_key 'id'
          expect(data['id']).to eq vocabulary.id
        end
      end

      context 'when view permission is false' do
        it 'does not return vocabulary through the user permissions' do
          vocabulary = FactoryGirl.create(:vocabulary,
                                          enabled_for_public_view: false)
          Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                                     view: false,
                                                                     vocabulary: vocabulary)

          data = client.get.relation('vocabularies').get.data['vocabularies']

          expect(data.count).to be_zero
        end

        it 'does not return vocabulary through the group permissions' do
          vocabulary = FactoryGirl.create(:vocabulary,
                                          enabled_for_public_view: false)
          group = FactoryGirl.create :group
          group.users << user
          Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                                      view: false,
                                                                      vocabulary: vocabulary)

          data = client.get.relation('vocabularies').get.data['vocabularies']

          expect(data.count).to be_zero
        end
      end
    end
  end
end
