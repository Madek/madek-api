require 'spec_helper'

describe 'filtering collections' do
  let :collections_relation do
    client.get.relation('collections')
  end

  def get_collections(filter = nil)
    collections_relation.get(filter).data['collections']
  end

  context 'by collection_id' do
    include_context :json_roa_client_for_authenticated_user do
      it 'as single filter option' do
        @collection = FactoryGirl.create(:collection)
        5.times do
          @collection.collections << FactoryGirl.create(:collection)
        end
        get_collections('collection_id' => @collection.id)
          .each do |c|
          collection = Collection.unscoped.find(c['id'])
          expect(@collection.collections).to include collection
        end
      end

      it 'combined with other filter option' do
        @collection = FactoryGirl.create(:collection)
        collection_1 = FactoryGirl.create(:collection,
                                           get_metadata_and_previews: false)
        collection_2 = FactoryGirl.create(:collection,
                                           get_metadata_and_previews: true)
        collection_3 = FactoryGirl.create(:collection,
                                           get_metadata_and_previews: false)
        collection_3.user_permissions << \
          FactoryGirl.create(:collection_user_permission,
                             user: user,
                             get_metadata_and_previews: true)
        [collection_1, collection_2, collection_3].each do |c|
          @collection.collections << c
        end

        response = get_collections('collection_id' => @collection.id,
                                   'me_get_metadata_and_previews' => true)
        expect(response.count).to be == 2
        response.each do |me|
          collection = Collection.unscoped.find(me['id'])
          expect(collection).not_to be == collection_1
          expect(@collection.collections).to include collection
        end
      end
    end
  end
end
