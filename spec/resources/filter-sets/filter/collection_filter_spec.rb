require 'spec_helper'

describe 'filtering filter_sets' do
  let :filter_sets_relation do
    client.get.relation('filter-sets')
  end

  def get_filter_sets(filter = nil)
    filter_sets_relation.get(filter).data['filter-sets']
  end

  context 'by collection_id' do
    include_context :json_roa_client_for_authenticated_user do
      it 'as single filter option' do
        @collection = FactoryGirl.create(:collection)
        5.times do
          @collection.filter_sets << FactoryGirl.create(:filter_set)
        end
        get_filter_sets('collection_id' => @collection.id)
          .each do |fs|
          filter_set = FilterSet.unscoped.find(fs['id'])
          expect(@collection.filter_sets).to include filter_set
        end
      end

      it 'combined with other filter option' do
        @collection = FactoryGirl.create(:collection)
        filter_set_1 = FactoryGirl.create(:filter_set,
                                           get_metadata_and_previews: false)
        filter_set_2 = FactoryGirl.create(:filter_set,
                                           get_metadata_and_previews: true)
        filter_set_3 = FactoryGirl.create(:filter_set,
                                           get_metadata_and_previews: false)
        filter_set_3.user_permissions << \
          FactoryGirl.create(:filter_set_user_permission,
                             user: user,
                             get_metadata_and_previews: true)
        [filter_set_1, filter_set_2, filter_set_3].each do |fs|
          @collection.filter_sets << fs
        end

        response = get_filter_sets('collection_id' => @collection.id,
                                   'me_get_metadata_and_previews' => true)
        expect(response.count).to be == 2
        response.each do |fs|
          filter_set = FilterSet.unscoped.find(fs['id'])
          expect(filter_set).not_to be == filter_set_1
          expect(@collection.filter_sets).to include filter_set
        end
      end
    end
  end
end
