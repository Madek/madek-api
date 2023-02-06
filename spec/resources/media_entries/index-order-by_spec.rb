require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'ordering media entries' do
  include_context :bunch_of_media_entries

  include_context :json_roa_client_for_authenticated_user do
    let :media_entries_relation do
      media_entries # force evaluation
      collection # force evaluation
      client.get.relation('media-entries')
    end

    def resource(order = nil)
      media_entries_relation.get('order' => order)
    end

    context "old style string 'order' attribute" do
      context 'when passed order is incorrect' do
        it 'raises an error' do
          response = resource('incorrect_value').response

          expect(response.status).to eq(422)
          expect(response.body).to eq({ 'message' => 'only the following values are allowed as '\
                                                     'order parameter: desc, asc, title_asc, '\
                                                     'title_desc, last_change, manual_asc, '\
                                                     'manual_desc and stored_in_collection' })
        end
      end

      context 'created_at' do
        include_examples 'ordering by created_at'

        [nil, 'asc', 'desc'].each do |direction|
          it "returns 30 media entries for #{direction.inspect} order" do
            expect(media_entries_created_at(direction).size).to eq(30)
          end
        end

        specify 'ascending order by default' do
          media_entries_created_at.each_cons(2) do |ca_pair|
            expect(ca_pair.first < ca_pair.second).to be true
          end
        end
      end

      context 'madek_core:title' do
        include_examples 'ordering by madek_core:title'

        it 'returns 30 media entries for ascending order' do
          expect(titles('asc').size).to eq(30)
        end

        it 'returns 30 media entries for descending order' do
          expect(titles('desc').size).to eq(30)
        end
      end

      context 'last_change' do
        include_examples 'ordering by last_change'

        it 'returns 30 media entries of ascending order' do
          expect(edit_session_updated_ats.size).to eq(30)
        end
      end

      context 'stored_in_collection' do
        context 'when collection_id param is missing' do
          it 'raises an error' do
            response = media_entries_relation.get(order: 'stored_in_collection').response
            expect(response.status).to eq(422)
            expect(response.body).to eq({ 'message' => 'collection_id param must be given' })
          end
        end
      end

      context 'manual_asc' do
        context 'when collection_id param is missing' do
          it 'raises an error' do
            response = media_entries_relation.get(order: 'manual_asc').response
            expect(response.status).to eq(422)
            expect(response.body).to eq({ 'message' => 'collection_id param must be given' })
          end
        end
      end

      context 'manual_desc' do
        context 'when collection_id param is missing' do
          it 'raises an error' do
            response = media_entries_relation.get(order: 'manual_desc').response
            expect(response.status).to eq(422)
            expect(response.body).to eq({ 'message' => 'collection_id param must be given' })
          end
        end
      end
    end

  end

  context "ordering media-entries in a particular set" do

    context "response of ordering by the order attribute of the arc descending" do

      let :response do
        media_entries_relation.get(
          collection_id: collection.id,
          order: [ [:arc, :order, :desc],
                   [:arc, :created_at, :desc],
                   [:media_entry, :created_at, :desc]].to_json).response
      end

      it "is OK" do
        expect(response.status).to be== 200
      end

      describe "arcs" do

        let :arcs do
          response.body.with_indifferent_access[:arcs]
        end

        let :arcs_before_first_order_nil do
          arcs.take_while{|a| a[:order]}
        end

        it "are present" do
          expect(arcs).not_to be_empty
        end

        it "nils come last" do
          arcs_tail_order_nils = arcs.reverse.take_while{|a| a[:order].nil?}.reverse
          expect(arcs).to be== arcs_before_first_order_nil + arcs_tail_order_nils
        end

        it "non order nil arcs are ordered descending" do
          expect(arcs_before_first_order_nil).to be==
            arcs_before_first_order_nil.sort_by{|arc| arc[:order] ? arc[:order] : -1 }.reverse
        end

        describe "child media entries" do

          let :child_media_entries do
            response.body.with_indifferent_access["media-entries"]
          end

          it "are exactly ordered like the arcs" do
            expect(child_media_entries.map{|me| me[:id]}).to be==
              arcs.map{|arc| arc[:media_entry_id]}
          end
        end
      end
    end


    context "response of ordering by the time media entries are added to the set" do

      let :response do
        media_entries_relation.get(
          collection_id: collection.id,
          order: [ [:arc, :created_at, :asc],
                   [:media_entry, :created_at, :desc]].to_json).response
      end


      describe "arcs" do

        let :arcs do
          response.body.with_indifferent_access[:arcs]
        end


        it "arcs are ordered by creation date ascending" do
          expect(arcs).to be==  arcs.sort_by{|arc| arc[:created_at]}
        end
      end
    end


    context "a title for each of the entries" do

      let :meta_key_title do
        FactoryBot.create :meta_key_title
      end

      let! :titles do
        media_entries.map do |me|
          @meta_datum_text = FactoryBot.create :meta_datum_text,
            media_entry: me, meta_key: meta_key_title
        end
      end

      # NOTE: meta_data should have ref to media_entry; so maybe we dont need the dance
      # with the additional meta_data fields in the response!

      context "response of ordering by metadatum string (title usually)" do

        let :response do
          media_entries_relation.get(
            collection_id: collection.id,
            order: [["MetaDatum::Text", meta_key_title.id, :asc]].to_json).response
        end

        it "has success http state" do
          expect(response.status).to be== 200
        end

        specify "media_entries are ordered by metadatum string" do
          media_entry_ids_from_response = response
            .body
            .with_indifferent_access["media-entries"]
            .map{|me| me[:id]}
          media_entry_ids_ordered_by_titles = titles \
            .filter{|t| media_entry_ids_from_response.include? t[:media_entry_id] }\
            .sort_by{|t| t[:string]}.map{|t| t[:media_entry_id]}

          expect(media_entry_ids_ordered_by_titles.to_a.length)
            .to eq media_entry_ids_from_response.to_a.length

          expect(media_entry_ids_ordered_by_titles.to_a.sort)
            .to be== media_entry_ids_from_response.to_a.sort
        end
      end
    end

    context 'ordering by order param' do
      def resource(order)
        media_entries_relation.get(
          collection_id: collection.id,
          order: order
        )
      end

      context 'created_at' do
        include_examples 'ordering by created_at'
      end

      context 'madek_core:title' do
        include_examples 'ordering by madek_core:title'
      end

      context 'last_change' do
        include_examples 'ordering by last_change'
      end

      context 'manual' do
        def update_arcs_with_positions
          collection
            .collection_media_entry_arcs
            .to_a
            .shuffle
            .each_with_index
            .map { |arc, index| arc.update!(position: index); arc }
        end

        before { @arcs = update_arcs_with_positions }

        it 'returns shuffled entries every time' do
          expect(update_arcs_with_positions.map(&:media_entry_id))
            .not_to eq(update_arcs_with_positions.map(&:media_entry_id))
        end

        context 'ascending order' do
          it 'returns media entries in the correct order' do
            media_entry_ids = resource('manual_asc').data['media-entries'].map { |me| me['id'] }

            expect(@arcs.map(&:media_entry_id)).to eq(media_entry_ids)
          end
        end

        context 'descending order' do
          it 'returns media entries in the correct order' do
            media_entry_ids = resource('manual_desc').data['media-entries'].map { |me| me['id'] }

            expect(@arcs.map(&:media_entry_id)).to eq(media_entry_ids.reverse)
          end
        end
      end

      context 'stored_in_collection' do
        def resource(*)
          media_entries_relation.get(
            collection_id: collection.id,
            order: 'stored_in_collection'
          )
        end

        context 'when collection has created_at ASC sorting' do
          before { collection.update!(sorting: 'created_at ASC') }

          include_examples 'ordering by created_at', 'asc'
        end

        context 'when collection has created_at DESC sorting' do
          before { collection.update!(sorting: 'created_at DESC') }

          include_examples 'ordering by created_at', 'desc'
        end

        context 'when collection has title ASC sorting' do
          before { collection.update!(sorting: 'title ASC') }

          include_examples 'ordering by madek_core:title', 'asc'
        end

        context 'when collection has title DESC sorting' do
          before { collection.update!(sorting: 'title DESC') }

          include_examples 'ordering by madek_core:title', 'desc'
        end

        context 'when collection has last_change sorting' do
          before { collection.update!(sorting: 'last_change') }

          include_examples 'ordering by last_change'
        end
      end
    end

    context 'default order ~> created_at ASC' do
      def resource(*)
        media_entries_relation.get(collection_id: collection.id)
      end

      include_examples 'ordering by created_at', 'asc'
    end
  end
end
